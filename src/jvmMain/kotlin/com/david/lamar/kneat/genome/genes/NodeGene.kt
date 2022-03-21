package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.attributes.ActivationAttribute
import com.david.lamar.kneat.genome.genes.attributes.AggregationAttribute
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import kotlin.math.abs

open class NodeGene(
    private val biasConfiguration: GenomeConfiguration.BiasConfiguration,
    private val responseConfiguration: GenomeConfiguration.ResponseConfiguration,
    private val activationConfiguration: GenomeConfiguration.ActivationConfiguration,
    private val aggregationConfiguration: GenomeConfiguration.AggregationConfiguration,
    attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>? = null
) : Gene() {

    override val managedAttributes = attributes ?: mapOf(
        BIAS_GENE to FloatAttribute(biasConfiguration),
        RESPONSE_GENE to FloatAttribute(responseConfiguration),
        ACTIVATION_GENE to ActivationAttribute(activationConfiguration),
        AGGREGATION_GENE to AggregationAttribute(aggregationConfiguration)
    )

    override fun distance(other: Gene): Float {
        managedAttributes.getOrElse(BIAS_GENE) { throw IllegalArgumentException() }
        val myBias = managedAttributes.getAs<FloatAttribute>(BIAS_GENE)
        val otherBias = other.managedAttributes.getAs<FloatAttribute>(BIAS_GENE)

        val myResponse = managedAttributes.getAs<FloatAttribute>(RESPONSE_GENE)
        val otherResponse = other.managedAttributes.getAs<FloatAttribute>(RESPONSE_GENE)

        val myActivation = managedAttributes.getAs<ActivationAttribute>(ACTIVATION_GENE)
        val otherActivation = other.managedAttributes.getAs<ActivationAttribute>(ACTIVATION_GENE)

        val myAggregation = managedAttributes.getAs<AggregationAttribute>(AGGREGATION_GENE)
        val otherAggregation = other.managedAttributes.getAs<AggregationAttribute>(AGGREGATION_GENE)

        var delta = abs(myBias.value - otherBias.value) + abs(myResponse.value - otherResponse.value)

        if (myActivation != otherActivation) {
            delta += 1f
        }

        if (myAggregation != otherAggregation) {
            delta += 1f
        }

        return delta
    }

    override fun copy(
        attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>
    ): Gene {
        return NodeGene(
            biasConfiguration = biasConfiguration,
            responseConfiguration = responseConfiguration,
            activationConfiguration = activationConfiguration,
            aggregationConfiguration = aggregationConfiguration,
            attributes = attributes
        )
    }

    companion object {
        const val BIAS_GENE = "bias"
        const val RESPONSE_GENE = "response"
        const val ACTIVATION_GENE = "activation"
        const val AGGREGATION_GENE = "aggregation"
    }
}
