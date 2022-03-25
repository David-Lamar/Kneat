package kneat.evolution.network

import kneat.evolution.genome.genes.ConnectionGene
import kneat.evolution.genome.genes.attributes.BooleanAttribute
import kneat.evolution.genome.genes.attributes.FloatAttribute
import kneat.util.getAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A transfer vessel for values between two nodes; this imitates the axons and dendrites of neurons in
 * biological neural networks.
 *
 * @property id The unique identifier for this connection
 * @property gene The gene that manages the attributes of this connection
 */
class Connection(
    override val id: Pair<Long, Long>,
    override val gene: ConnectionGene
) : NetworkElement<Pair<Long, Long>, ConnectionGene>() {

    /**
     * The coroutine scope used to execute expensive operations inside of a connection
     *
     * TODO: Either a reporter or exception handler should be added to this scope
     */
    //TODO: This and the Network and Node scopes should be linked, and the number of threads
    // should be configurable in the Configuration. When this is changed, please update
    // the documentation in Network
    private val connectionScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Unconfined
    )

    /**
     * The mutable flow used to represent the "value" of this connection
     */
    private val _valueFlow = MutableStateFlow(0f)

    /**
     * Used to subscribe to all updates from this connection. Connections and nodes act asynchronously
     * to more closely imitate how biological neural networks behave. This has some implications to how
     * a Kneat neural network behaves; see [Network] for more information on this.
     */
    val valueFlow: StateFlow<Float> = _valueFlow

    /**
     * Triggered any time a value is posted by a node this connection has subscribed to; see [registerTrigger].
     *
     * The value emitted into valueFlow will be equal to the [value] multiplied by the weight (managed by the
     * [gene]) or 0 if not enabled (also managed by the [gene])
     */
    private suspend fun activate(value: Float) { //TODO: Should this have a withContext? need to investigate.
        val weight = gene.managedAttributes.getAs<FloatAttribute>(ConnectionGene.WEIGHT_GENE).value
        val enabled = gene.managedAttributes.getAs<BooleanAttribute>(ConnectionGene.ENABLED_GENE).value
        _valueFlow.emit(if (enabled) value * weight else 0f)
    }

    /**
     * Triggers an [activate] of this connection when the [trigger] posts a value
     */
    fun registerTrigger(trigger: StateFlow<Float>) {
        connectionScope.launch {
            trigger.collect { activate(it) }
        }
    }
}
