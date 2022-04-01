package kneat.evolution.genome.genes.configuration

import kneat.evolution.genome.genes.attributes.configuration.AttributeConfiguration
import kneat.evolution.genome.genes.attributes.configuration.BooleanConfiguration

/**
 * Used to configure how connections behave across a genome.
 *
 * @property default see [AttributeConfiguration]
 * @property mutationRate see [AttributeConfiguration]
 * @property replaceRate see [AttributeConfiguration]
 * @property additionProbability The probability that a connection will get added to the network on any given mutation
 * Defaulted to 0 to speed up evaluation (Connections automatically get added and deleted in crossover, this adds extra
 * connections and may result in larger, less dense networks.)
 * @property deletionProbability The probability that a connection will get deleted from the network on any given mutation
 * Defaulted to 0 to speed up evaluation (Connections automatically get added and deleted in crossover, this removes extra
 * connections and may result in larger, less dense networks.)
 * @property allowRecurrence Whether or not nodes can link to themselves OR may cause loops in the network. Note: If
 * this is set to false, there is a moderate computational penalty associated with calculating a connection addition
 * as it has to avoid causing cycles in the network. If you have a lot of nodes, a high [additionProbability], or
 * a slow computer it may be worth while allowing recurrence even if it's not explicitly necessary for your
 * problem domain.
 * problem domain. Defaults to true due to the above.
 * @property initialConnection The initial [ConnectionType] used when a connection is created
 */
data class ConnectionConfiguration(
    override val default: Boolean? = null,
    override val replaceRate: Float,
    override val mutationRate: Float,
    val additionProbability: Float = 0f,
    val deletionProbability: Float = 0f,
    val allowRecurrence: Boolean = true,
    val initialConnection: ConnectionType = ConnectionType.Unconnected
) : BooleanConfiguration()