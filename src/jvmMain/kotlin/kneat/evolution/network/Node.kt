package kneat.evolution.network

import kneat.evolution.genome.genes.NodeGene
import kneat.evolution.genome.genes.NodeGene.Companion.ACTIVATION_GENE
import kneat.evolution.genome.genes.NodeGene.Companion.AGGREGATION_GENE
import kneat.evolution.genome.genes.NodeGene.Companion.BIAS_GENE
import kneat.evolution.genome.genes.NodeGene.Companion.RESPONSE_GENE
import kneat.evolution.genome.genes.attributes.ActivationAttribute
import kneat.evolution.genome.genes.attributes.AggregationAttribute
import kneat.evolution.genome.genes.attributes.FloatAttribute
import kneat.util.getAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Node(
    override val id: Long,
    override val gene: NodeGene
) : NetworkElement<Long, NodeGene>() {

    /**
     * The coroutine scope used to execute expensive operations inside of a node
     *
     * TODO: Either a reporter or exception handler should be added to this scope
     */
    //TODO: This and the Network and Connection scopes should be linked, and the number of threads
    // should be configurable in the Configuration. When this is changed, please update
    // the documentation in Network
    private val nodeContext: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    /**
     * The mutable flow used to represent the "value" of this node
     */
    private val _valueFlow = MutableStateFlow(0f)

    /**
     * Used to subscribe to all updates from this node. Connections and nodes act asynchronously
     * to more closely imitate how biological neural networks behave. This has some implications to how
     * a Kneat neural network behaves; see [Network] for more information on this.
     */
    val valueFlow: StateFlow<Float> = _valueFlow

    /**
     * Triggered any time a value is posted by any connection this node has subscribed to; see [registerTrigger].
     *
     * The value emitted into [valueFlow] will be equal to the response (managed by the [gene]) plus the [Aggregation]
     * (managed by the [gene]) of all [values] multiplied by the Bias (managed by the [gene]) fed into the
     * [Activation] function (managed by the [gene]).
     */
    private suspend fun activate(values: List<Float>) { //TODO: Should this have a withContext? need to investigate.
        val aggregation = gene.managedAttributes.getAs<AggregationAttribute>(AGGREGATION_GENE).value
        val activation = gene.managedAttributes.getAs<ActivationAttribute>(ACTIVATION_GENE).value
        val response = gene.managedAttributes.getAs<FloatAttribute>(RESPONSE_GENE).value
        val bias = gene.managedAttributes.getAs<FloatAttribute>(BIAS_GENE).value
        val aggregated = aggregation.aggregate(values)

        _valueFlow.emit(activation.activate(response + aggregated * bias))
    }

    /**
     * Triggers an [activate] of this node when the [trigger] posts a value. This trigger represents
     * a flow of ALL connections coming into this node and will update any time any of the connections
     * post a value.
     */
    fun registerTrigger(trigger: StateFlow<List<Float>>) {
        nodeContext.launch {
            trigger.collect {
                activate(it)
            }
        }
    }
}