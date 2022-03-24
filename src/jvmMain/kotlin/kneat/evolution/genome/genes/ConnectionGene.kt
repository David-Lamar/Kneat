package kneat.evolution.genome.genes

import kneat.evolution.genome.genes.ConnectionGene.Companion.ENABLED_GENE
import kneat.evolution.genome.genes.ConnectionGene.Companion.WEIGHT_GENE
import kneat.evolution.genome.genes.attributes.Attribute
import kneat.evolution.genome.genes.attributes.BooleanAttribute
import kneat.evolution.genome.genes.attributes.FloatAttribute
import kneat.evolution.genome.genes.attributes.configuration.AttributeConfiguration
import kneat.evolution.genome.genes.attributes.configuration.WeightConfiguration
import kneat.evolution.genome.genes.configuration.ConnectionConfiguration
import kneat.evolution.network.Connection
import kneat.util.getAs
import kotlin.math.abs

/**
 * A [Gene] used to configure the attributes of a [Connection]
 *
 * @property weightConfiguration The [WeightConfiguration] used to configure the
 * [WEIGHT_GENE] attribute
 * @property connectionConfiguration The [ConnectionConfiguration] used to configure
 * the [ENABLED_GENE] attribute
 * @param attributes Values to replace the initial [managedAttributes]; primarily used in cases of
 * copying this immutably.
 */
class ConnectionGene(
    private val weightConfiguration: WeightConfiguration,
    private val connectionConfiguration: ConnectionConfiguration,
    attributes: Map<String, Attribute<out Any, out AttributeConfiguration<out Any>>>? = null
) : Gene() {

    /**
     * The attributes managed by this gene.
     *
     * [WEIGHT_GENE] is the multiplier that will be applied to a node's input when fed through this connection
     * [ENABLED_GENE] is the state of this connection itself; if false, the connection acts as if it's "off"; i.e.
     * a Weight of 0
     */
    override val managedAttributes = attributes ?: mapOf(
        WEIGHT_GENE to FloatAttribute(weightConfiguration),
        ENABLED_GENE to BooleanAttribute(connectionConfiguration)
    )

    /**
     * Computes the difference between another [ConnectionGene], [other].
     *
     * Distance is calculated as follows:
     * 1. We take the absolute difference between the two gene's [WEIGHT_GENE] values. For example:
     * Gene A has a weight value of .2
     * Gene B has a weight value of .8
     *
     * The distance between these two values is .6; so that is the "distance" between these two values
     * 2. Add "1" if the enabled states are different
     *
     * *Note*: If any other gene is passed in than a [ConnectionGene], you will get errors via the [getAs] function
     *
     * @return absolute difference between weights + 1 if the enabled states differ
     */
    override fun distance(other: Gene): Float {
        val myWeight = managedAttributes.getAs<FloatAttribute>(WEIGHT_GENE)
        val otherWeight = other.managedAttributes.getAs<FloatAttribute>(WEIGHT_GENE)

        val myEnabled = managedAttributes.getAs<BooleanAttribute>(ENABLED_GENE)
        val otherEnabled = other.managedAttributes.getAs<BooleanAttribute>(ENABLED_GENE)

        var delta = abs(myWeight.value - otherWeight.value)
        if (myEnabled.value != otherEnabled.value) delta += 1f

        return delta
    }

    override fun copy(
        attributes: Map<String, Attribute<out Any, out AttributeConfiguration<out Any>>>
    ): Gene {
        return ConnectionGene(
            weightConfiguration = weightConfiguration,
            connectionConfiguration = connectionConfiguration,
            attributes = attributes
        )
    }

    companion object {
        /**
         * Used to index an attribute that manages the weight field of this gene
         */
        const val WEIGHT_GENE = "weight"

        /**
         * Used to index an attribute that manages the enabled field of this gene
         */
        const val ENABLED_GENE = "enabled"
    }
}