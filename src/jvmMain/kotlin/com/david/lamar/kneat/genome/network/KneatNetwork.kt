package com.david.lamar.kneat.genome.network

import com.david.lamar.kneat.genome.Genome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class KneatNetwork(
    private val inputKeys: List<Long>,
    private val outputKeys: List<Long>,
    private val nodes: List<Node>,
    private val connections: List<Connection>,
) : Network {

    private val networkScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    private lateinit var initialFlows: Map<Long, MutableStateFlow<Float>>
    private lateinit var outputFlow: StateFlow<List<Float>>

    private suspend fun initialize() {
        val outputs = connections.groupBy { it.id.second }
        outputs.forEach { (nodeId, connectionsTo) ->
            val node = nodes.find { it.id == nodeId }

            val flow = combine(*connectionsTo.map { it.valueFlow }.toTypedArray()) {
                it.toList()
            }

            node?.registerTrigger(flow.stateIn(networkScope))
        }

        connections.forEach {
            val inputFrom = it.id.first

            val flow = if (inputKeys.contains(inputFrom)) {
                initialFlows[inputFrom] ?: error("")
            } else {
                nodes.find { node -> node.id == inputFrom }!!.valueFlow
            }

            it.registerTrigger(flow)
        }

        outputFlow = combine(*outputKeys.map { id ->
            nodes.find { it.id == id }!!.valueFlow
        }.toTypedArray()) {
            it.toList()
        }.stateIn(networkScope)
    }

    override suspend fun activate(inputs: List<Float>, stabilizationDelay: Long): List<Float> = withContext(networkScope.coroutineContext) {
        initialFlows = inputKeys.map {
            it to MutableStateFlow(inputs[inputKeys.indexOf(it)])
        }.toMap()

        initialize()

        inputKeys.forEachIndexed { index, key ->
            initialFlows[key]?.emit(inputs[index])
        }

        //TODO: Add some sort of stabilization logic here for recurrent networks
        if (outputFlow.value.size != outputKeys.size) {
            return@withContext outputFlow.first { it.size == outputKeys.size }
        } else {
            return@withContext outputFlow.value
        }
    }

    override fun reset() {
        // Do nothing, we're not recurrent (yet)
    }

    companion object {
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