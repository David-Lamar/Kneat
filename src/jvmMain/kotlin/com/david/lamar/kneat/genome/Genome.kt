package com.david.lamar.kneat.genome

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.ConnectionGene
import com.david.lamar.kneat.genome.genes.NodeGene
import com.david.lamar.kneat.genome.genes.attributes.BooleanAttribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import com.david.lamar.kneat.genome.genes.getAs
import com.david.lamar.kneat.genome.network.Activation
import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.genome.network.Connection
import com.david.lamar.kneat.genome.network.Node
import com.david.lamar.kneat.util.*
import com.david.lamar.kneat.util.reporting.Reporter
import kotlin.random.Random

open class Genome(
    val key: Long,
    val config: GenomeConfiguration,
    specifiedConnections: MutableList<Connection> = mutableListOf(),
    specifiedNodes: MutableList<Node> = mutableListOf(),
    specifiedReporters: MutableList<Reporter> = mutableListOf()
) {
    open val inputKeys = (-config.nodeConfiguration.inputs..-1L).toList().reversed()
    open val outputKeys: List<Long> = (1L..config.nodeConfiguration.outputs).toList()

    private var _connections: MutableList<Connection> = specifiedConnections
    val connections: List<Connection> = _connections

    private var _nodes: MutableList<Node> = specifiedNodes
    val nodes: List<Node> = _nodes

    private var _reporters: MutableList<Reporter> = specifiedReporters

    protected var activationList: MutableList<Activation> = mutableListOf(
        *config.activationConfig.available.toTypedArray()
    )

    protected var aggregationList: MutableList<Aggregation> = mutableListOf(
        *config.aggregationConfiguration.available.toTypedArray()
    )

    private val fitnessHistory: MutableMap<Long, Float> = mutableMapOf()

    init {
        initializeNodes()
        initializeConnections()
    }

    /******* Public Final API *****/

    fun addActivation(activation: Activation) {
        activationList.add(activation)
    }

    fun addAggregation(aggregation: Aggregation) {
        aggregationList.add(aggregation)
    }

    fun addReporter(reporter: Reporter) {
        _reporters.add(reporter)
    }

    /******* Public Open API *****/

    open fun mutate() {
        val allowedMutations = config.structureConfiguration.allowedStructuralMutations

        if (allowedMutations == 0) {
            //TODO: Should make this an annotation and use a custom annotation processor
            throw IllegalStateException()
        }

        val nodeAddProb = config.nodeConfiguration.additionProbability
        val nodeDelProb = config.nodeConfiguration.deletionProbability
        val connAddProb = config.connectionConfiguration.additionProbability
        val connDelProb = config.connectionConfiguration.deletionProbability

        val rand = Random.nextFloat()

        val mutateList = mutableListOf<() -> Unit>()

        if (rand < nodeAddProb) mutateList.add {
            if (_connections.size == 0 && config.structureConfiguration.ensureStructuralMutation) {
                mutateAddConnection()
            } else {
                mutateAddNode()
            }
        }

        if (rand < nodeDelProb) mutateList.add { mutateDeleteNode() }
        if (rand < connAddProb) mutateList.add { mutateAddConnection() }
        if (rand < connDelProb) mutateList.add { mutateDeleteConnection() }

        mutateList.shuffled().takeLast(allowedMutations).forEach { it() }

        _connections.forEach { connection ->
            connection.gene.managedAttributes.values.forEach { gene -> gene.mutate() }
        }

        _nodes.forEach { node ->
            node.gene.managedAttributes.values.forEach { gene -> gene.mutate() }
        }
    }

    open fun createNew(
        key: Long,
        config: GenomeConfiguration,
        connections: MutableList<Connection> = mutableListOf(),
        nodes: MutableList<Node> = mutableListOf(),
        reporters: MutableList<Reporter> = mutableListOf()
    ) : Genome {
        return Genome(
            key = key,
            config = config,
            specifiedConnections = connections,
            specifiedNodes = nodes,
            specifiedReporters = reporters
        )
    }

    open suspend fun evaluate(evaluationFunction: EvaluationFunction, generation: Long) : Float {
        val fitness = evaluationFunction(this)
        fitnessHistory[generation] = fitness
        return fitness
    }

    // -1 = latest
    open fun getFitness(generation: Long = -1) : Float {
        val key = if (generation < 0) fitnessHistory.keys.maxOfOrNull { it } ?: -1 else generation
        return fitnessHistory.getOrElse(key) { error("Attempted to retrieve fitness history for $key however, $key was not found.") }
    }
    
    open fun distance(other: Genome) : Float {
        val cacheKey = Pair(this.key, other.key)

        val cacheValue = cachedDistances.getOrDefault(cacheKey, null)

        if (cacheValue != null) {
            cacheHits++
            return cacheValue
        } else {
            cacheMisses++

            val compKey = Pair(other.key, this.key)

            val nodeDistance = _nodes.distance(
                to = other._nodes,
                weightCoefficient = config.compatibilityConfiguration.weightCoefficient,
                disjointCoefficient = config.compatibilityConfiguration.disjointCoefficient
            )

            val connectionDistance = _connections.distance(
                to = other._connections,
                weightCoefficient = config.compatibilityConfiguration.weightCoefficient,
                disjointCoefficient = config.compatibilityConfiguration.disjointCoefficient
            )

            val distance = nodeDistance + connectionDistance
            cachedDistances[cacheKey] = distance
            cachedDistances[compKey] = distance
            return distance
        }
    }

    open fun size() : Int {
        val enabledConnections = _connections.count { it.gene.managedAttributes.getAs<BooleanAttribute>(ConnectionGene.ENABLED_GENE).value }
        return _nodes.size + enabledConnections
    }

    /******* Private default implementation for a genome *****/

    private fun initializeNodes() {
        // Add output nodes (Note: input nodes not needed since they aren't real "nodes" with activations, etc.
        outputKeys.forEach {
            _nodes.add(createNode(it, config))
        }

        // Add hidden nodes if configured
        for (hidden in 0 until config.nodeConfiguration.initialHidden) {
            _nodes.add(createNode(getNewNodeKey(), config))
        }
    }

    private fun getNewNodeKey() : Long {
        return _nodes.maxOf { it.id } + 1
    }

    private fun initializeConnections() {
        val allNodes = _nodes.map { it.id }
        val hiddenNodes = allNodes.filter { !outputKeys.contains(it) }
        val initialConnection = config.connectionConfiguration.initialConnection
        val (inputs, outputs) = when(initialConnection) {
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.Unconnected -> {
                listOf(emptyList(), emptyList())
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.SingleSelection.All -> {
                listOf(listOf(inputKeys.random()), allNodes)
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.SingleSelection.HiddenOnly -> {
                listOf(
                    listOf(inputKeys.random()),
                    if (hiddenNodes.isEmpty()) {
                        _reporters.report().warn(
                            WARNING_INITIAL_CONFIGURATION.format(
                                "SingleSelection.HiddenOnly",
                                "SingleSelection.All"
                            ))
                        allNodes
                    } else {
                        hiddenNodes
                    }
                )
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.SingleSelection.OutputOnly -> {
                listOf(listOf(inputKeys.random()), outputKeys)
            }
            is GenomeConfiguration.ConnectionConfiguration.ConnectionType.PartialSelection.All -> {
                listOf(
                    inputKeys.filter { Random.nextFloat() < initialConnection.probability },
                    allNodes
                )
            }
            is GenomeConfiguration.ConnectionConfiguration.ConnectionType.PartialSelection.HiddenOnly -> {
                listOf(
                    inputKeys.filter { Random.nextFloat() < initialConnection.probability },
                    if (hiddenNodes.isEmpty()) {
                        _reporters.report().warn(
                            WARNING_INITIAL_CONFIGURATION.format(
                                "PartialSelection.HiddenOnly",
                                "PartialSelection.All"
                            ))
                        allNodes
                    } else {
                        hiddenNodes
                    }
                )
            }
            is GenomeConfiguration.ConnectionConfiguration.ConnectionType.PartialSelection.OutputOnly -> {
                listOf(
                    inputKeys.filter { Random.nextFloat() < initialConnection.probability },
                    outputKeys
                )
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.FullSelection.All -> {
                listOf(inputKeys, allNodes)
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.FullSelection.HiddenOnly -> {
                listOf(
                    inputKeys,
                    if (hiddenNodes.isEmpty()) {
                        _reporters.report().warn(
                            WARNING_INITIAL_CONFIGURATION.format(
                                "FullSelection.HiddenOnly",
                                "FullSelection.All"
                            ))
                        allNodes
                    } else {
                        hiddenNodes
                    }
                )
            }
            GenomeConfiguration.ConnectionConfiguration.ConnectionType.FullSelection.OutputOnly -> {
                listOf(inputKeys, outputKeys)
            }
        }

        outputs.forEach { output ->
            inputs.forEach { input ->
                _connections.add(createConnection(newKey = Pair(input, output), config = config))
            }
        }
    }

    private fun mutateAddNode() {
        val toSplit = _connections.random()
        val toSplitWeight = toSplit.gene.managedAttributes.getAs<FloatAttribute>(ConnectionGene.WEIGHT_GENE).value
        val newNodeId = getNewNodeKey()
        val newNode = createNode(
            nodeId = newNodeId,
            config = config
        )

        _nodes.add(newNode)
        _connections.replaceAll {
            if (it.id == toSplit.id) {
                createConnection(it.id, config = config, enabled = false)
            } else {
                it
            }
        }

        val (input, output) = toSplit.id

        _connections.addAll(
            createConnection(Pair(input, newNodeId), config = config, weight = 1f, enabled = true),
            createConnection(Pair(newNodeId, output), config = config, weight = toSplitWeight, enabled = true)
        )
    }

    private fun mutateDeleteNode() {
        val deleteList = _nodes.filter { !outputKeys.contains(it.id) }
        if (deleteList.isEmpty()) return

        val deleteNode = deleteList.random()
        _nodes.remove(deleteNode)
        _connections.removeIf {
            it.id.first == deleteNode.id || it.id.second == deleteNode.id
        }
    }

    //TODO: Expensive operations -- Parallelize
    private fun getInputId(toOutputId: Long) : Long? {
        var inputList = (_nodes.map { it.id } + inputKeys)

        val outputIsTerminal = outputKeys.contains(toOutputId)

        // If our output is an output key, we don't a connection going to another output
        if (outputIsTerminal) inputList = inputList.filter { !outputKeys.contains(it) }

        if (!config.connectionConfiguration.allowRecurrence && !outputIsTerminal) {
            _reporters.report().info("Calculating connection and ensuring loop safety... (If a feed-forward network isn't necessary, you can speed this up by allowing recurrence.)")

            // We want to remove ALL nodes that output could possibly go INTO. Make sure we don't have loops
            val relatedNodes = mutableListOf<Long>()
            var checkList = listOf(toOutputId)

            while (checkList.isNotEmpty()) {
                val nextCheck = mutableListOf<Long>()

                checkList.forEach { checkId ->
                    if (!relatedNodes.contains(checkId)) relatedNodes.add(checkId)

                    nextCheck.addAll(
                        _connections
                            .filter { it.id.first == checkId
                                    && !relatedNodes.contains(it.id.second)
                                    && !nextCheck.contains(it.id.second) }
                            .map { it.id.second }
                    )
                }

                checkList = nextCheck
            }

            inputList = inputList.filter { relatedNodes.contains(it) }
        }

        return if (inputList.isEmpty()) null else inputList.random()
    }

    //TODO: Expensive operations -- Parallelize
    private fun mutateAddConnection() {
        val ensureStructuralMutation = config.structureConfiguration.ensureStructuralMutation

        val availableOutputList = _nodes.map { it.id }.toMutableList()
        var outputId: Long
        var existingConnectionEnabled : Boolean? = null
        var newId: Pair<Long, Long>? = null

        // Guarantee a connection is found based on our config criteria
        do {
            outputId = availableOutputList.random()
            availableOutputList.remove(outputId) // Narrow our potential search for next time, if needed

            getInputId(outputId)?.let { inputId ->
                newId = Pair(inputId, outputId)
                val existingConnection = _connections.find { it.id == newId}
                existingConnectionEnabled = existingConnection
                    ?.gene
                    ?.managedAttributes
                    ?.getAs<BooleanAttribute>(ConnectionGene.ENABLED_GENE)
                    ?.value
            }
        } while (newId == null || (ensureStructuralMutation && existingConnectionEnabled == true))

        val finalizedId = newId ?: return

        if (ensureStructuralMutation && existingConnectionEnabled == false) {
            ensureStableStructure(finalizedId)
            return // We've made a "mutation" by enabling a connection, so we can exit
        }

        _connections.add(createConnection(finalizedId, config))
    }

    private fun ensureStableStructure(newId: Pair<Long, Long>) : Boolean {
        var foundMatch = false
        _connections.replaceAll {
            if (it.id == newId) {
                foundMatch = true
                createConnection(it.id, enabled = true, config = config)
            } else {
                it
            }
        }

        return foundMatch
    }

    private fun mutateDeleteConnection() {
        _connections.removeRandom()
    }

    companion object {
        private var cachedDistances: MutableMap<Pair<Long, Long>, Float> = mutableMapOf()
        private var cacheHits: Long = 0
        private var cacheMisses: Long = 0

        suspend fun createFromCrossover(
            newId: Long,
            first: Genome,
            other: Genome,
        ) : Genome {
            val (parent1, parent2) = listOf(first, other).sortedBy { it.getFitness() }

            val newConnections = parent1._connections.mapParallel { p1c ->
                val p2Connect = parent2._connections.find { p2c -> p2c.id == p1c.id }

                if (p2Connect == null) {
                    p1c
                } else {
                    val newGene = p1c.gene.crossover(p1c.gene)
                    Connection(p1c.id, newGene as ConnectionGene)
                }
            }

            val newNodes = parent1._nodes.mapParallel { p1n ->
                val p2Node = parent2._nodes.find { p2c -> p2c.id == p1n.id }

                if (p2Node == null) {
                    p1n
                } else {
                    val newGene = p1n.gene.crossover(p2Node.gene)
                    Node(p1n.id, newGene as NodeGene)
                }
            }

            return Genome(
                key = newId,
                config = parent1.config,
                specifiedConnections = newConnections.toMutableList(),
                specifiedNodes = newNodes.toMutableList(),
                specifiedReporters = parent1._reporters
            )
        }

       fun createNode(
           nodeId: Long,
           config: GenomeConfiguration
       ) : Node {
           val gene = NodeGene(
               config.biasConfiguration,
               config.responseConfiguration,
               config.activationConfig,
               config.aggregationConfiguration
           )
            return Node(nodeId, gene)
        }

        fun createConnection(
            newKey: Pair<Long, Long>,
            config: GenomeConfiguration,
            weight: Float? = null,
            enabled: Boolean? = null
        ) : Connection {
            val gene = ConnectionGene(
                config.weightConfiguration.copy(default = weight ?: config.weightConfiguration.default),
                config.connectionConfiguration.copy(default = enabled ?: config.connectionConfiguration.default)
            )

            return Connection(newKey, gene)
        }
    }
}