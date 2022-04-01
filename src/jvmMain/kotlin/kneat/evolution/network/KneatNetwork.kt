package kneat.evolution.network

import kneat.evolution.genome.Genome
import kneat.evolution.genome.genes.ConnectionGene
import kneat.evolution.genome.genes.NodeGene
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class KneatNetwork(
    private val inputKeys: List<Long>,
    private val outputKeys: List<Long>,
    private val nodes: List<Pair<Long, NodeGene>>,
    private val connections: List<Pair<Pair<Long, Long>, ConnectionGene>>,
) : Network {

    private val networkScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    class Node(
        private val nodeScope: CoroutineScope,
        private val gene: NodeGene,
        private val activationThreshold: Float = 1f, // Percentage of connections to accumulate; rounded down
        private val refractoryPeriod: Long = 1000000 // Nanoseconds
    ) {
        //private val nodeScope: CoroutineScope = CoroutineScope(context + Job())
        private var connections = AtomicInteger(0)
        private var connectionThreshold = AtomicInteger(0)

        private val collectionCount = AtomicInteger(0)
        private val collectionList = AtomicReference<List<Float>>(listOf())
        private val refractoryPeriodStartTime = AtomicLong(0)

        private val _nodeFlow = MutableStateFlow(0f)
        val nodeFlow: StateFlow<Float> = _nodeFlow

        private suspend fun activate(input: List<Float>) {
            val agg = gene.getAggregation().aggregate(input)
            val activated = gene.getActivation().activate(gene.getResponse() + agg * gene.getBias())
            _nodeFlow.emit(activated)
        }

        private suspend fun collect(value: Float) {
            val newCount = collectionCount.incrementAndGet()
            val collectionList = collectionList.updateAndGet {
                listOf(*it.toTypedArray(), value)
            }

            if (newCount >= connectionThreshold.get() && canActivate()) {
                activate(collectionList.toList())
            }
        }

        private fun canActivate() : Boolean {
            return System.nanoTime() > (refractoryPeriodStartTime.get() + refractoryPeriod)
        }

        suspend fun registerTrigger(flow: StateFlow<Float>) {
            val connections = connections.incrementAndGet()
            connectionThreshold.set((connections * activationThreshold).toInt())

            try {
                nodeScope.launch {
                    flow.collect {
                        collect(it)
                    }
                }
            } catch (_ : Exception) {
                // Everything's perfectly alright now... We're fine.. We're all fine here ... Now. Thank you. How are you?
            }
        }
    }

    class Connection(
        private val connectionScope: CoroutineScope,
        private val gene: ConnectionGene
    ) {
        //private val connectionScope: CoroutineScope = CoroutineScope(context + Job())

        private val _connectionFlow = MutableStateFlow(0f)
        val connectionFlow: StateFlow<Float> = _connectionFlow

        suspend fun registerTrigger(flow: StateFlow<Float>) {
            try {
                connectionScope.launch {
                    flow.collect {
                        _connectionFlow.emit(if (gene.isEnabled()) gene.getWeight() * it else 0f)
                    }
                }
            } catch (_: Exception) {
                // Everything's perfectly alright now... We're fine.. We're all fine here ... Now. Thank you. How are you?
            }

        }
    }

    private val inputFlows: Map<Long, List<Connection>>
    private val outputFlows: Map<Long, StateFlow<Float>>

    init {
        val connectionFlows: MutableMap<Pair<Long, Long>, Connection> = mutableMapOf()
        val connectionFlowsTo: MutableMap<Long, MutableList<Connection>> = mutableMapOf()
        val connectionFlowsFrom: MutableMap<Long, MutableList<Connection>> = mutableMapOf()

        connections.forEach {
            val to = it.first.second
            val from = it.first.first
            val connection = Connection(
                networkScope,
                it.second
            )

            connectionFlows[it.first] = connection
            connectionFlowsTo[to] = (connectionFlowsTo[to] ?: mutableListOf()).apply { add(connection) }
            connectionFlowsFrom[from] = (connectionFlowsFrom[from] ?: mutableListOf()).apply { add(connection) }
        }

        val nodeFlows = nodes.map {
            it.first to Node(
                networkScope,
                it.second
            )
        }.toMap()

        // This links all of the nodes and connections together
        runBlocking {
            nodeFlows.forEach { (id, node) ->
                val connectionsIn = connectionFlowsTo[id] ?: emptyList()
                val connectionsOut = connectionFlowsTo[id] ?: emptyList()

                connectionsIn.forEach { conn ->
                    node.registerTrigger(conn.connectionFlow)
                }

                connectionsOut.forEach {conn ->
                    conn.registerTrigger(node.nodeFlow)
                }
            }

        }

        inputFlows = inputKeys.map {
            it to (connectionFlowsFrom[it] ?: emptyList())
        }.toMap().toSortedMap()

        outputFlows = outputKeys.map {
            it to (nodeFlows[it] ?: error("")).nodeFlow
        }.toMap().toSortedMap()
    }

    private suspend fun setInputFlows(inputs: List<Float>) {
        inputFlows.values.forEachIndexed { index, connList ->
            connList.forEach { testConnection ->
                testConnection.registerTrigger(MutableStateFlow(inputs[index]))
            }
        }
    }

    private suspend fun getOutputs() : List<Float> {
        return withContext(networkScope.coroutineContext + Job()) {
            combine(*outputFlows.values.toTypedArray()) {
                it.toList()
            }.first {
                it.size == outputKeys.size && !it.all { value -> value == 0f }
            }
        }
    }

    override suspend fun activate(inputs: List<Float>, stabilizationDelay: Long): List<Float> {
        setInputFlows(inputs)
        return getOutputs()
    }

    fun clean() {
        networkScope.coroutineContext.cancelChildren()
    }

    companion object {
        /**
         * Creates a TestNetwork with the provided [genome]
         */
        fun create(genome: Genome) : Network {
            return KneatNetwork(
                genome.inputKeys,
                genome.outputKeys,
                genome.nodes,
                genome.connections
            )
        }
    }
}