package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.ConnectionGene.Companion.ENABLED_GENE
import com.david.lamar.kneat.genome.genes.ConnectionGene.Companion.WEIGHT_GENE
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import com.david.lamar.kneat.genome.genes.attributes.BooleanAttribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import kotlin.math.abs

/**
 * A [Gene] used to configure the attributes of a [com.david.lamar.kneat.genome.network.Connection]
 *
 * @property weightConfiguration The [GenomeConfiguration.WeightConfiguration] used to configure the
 * [WEIGHT_GENE] attribute
 * @property connectionConfiguration The [GenomeConfiguration.ConnectionConfiguration] used to configure
 * the [ENABLED_GENE] attribute
 * @param attributes Values to replace the initial [managedAttributes]; primarily used in cases of
 * copying this immutably.
 */
class ConnectionGene(
    private val weightConfiguration: GenomeConfiguration.WeightConfiguration,
    private val connectionConfiguration: GenomeConfiguration.ConnectionConfiguration,
    attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>? = null
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
        attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>
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