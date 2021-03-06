package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.attributes.ActivationAttribute
import com.david.lamar.kneat.genome.genes.attributes.AggregationAttribute
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import kotlin.math.abs

/**
 * A [Gene] used to configure the attributes of a [com.david.lamar.kneat.genome.network.Node]
 *
 * @property biasConfiguration The [GenomeConfiguration.BiasConfiguration] used to configure the
 * [BIAS_GENE] attribute
 * @property responseConfiguration The [GenomeConfiguration.ResponseConfiguration] used to configure
 * the [RESPONSE_GENE] attribute
 * @property activationConfiguration The [GenomeConfiguration.ActivationConfiguration] used to configure the
 * [ACTIVATION_GENE] attribute
 * @property aggregationConfiguration The [GenomeConfiguration.AggregationConfiguration] used to configure
 * the [AGGREGATION_GENE] attribute
 * @param attributes Values to replace the initial [managedAttributes]; primarily used in cases of
 * copying this immutably.
 */
open class NodeGene(
    private val biasConfiguration: GenomeConfiguration.BiasConfiguration,
    private val responseConfiguration: GenomeConfiguration.ResponseConfiguration,
    private val activationConfiguration: GenomeConfiguration.ActivationConfiguration,
    private val aggregationConfiguration: GenomeConfiguration.AggregationConfiguration,
    attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>? = null
) : Gene() {

    /**
     * The attributes managed by this gene.
     *
     * [BIAS_GENE] is the multiplier that will be applied to a Node's output after activation
     * [RESPONSE_GENE] is the addend applied to a Node's output after activation
     * [ACTIVATION_GENE] is the [com.david.lamar.kneat.genome.network.Activation] function used by the Node
     * [AGGREGATION_GENE] is the [com.david.lamar.kneat.genome.network.Aggregation] function used by the node
     */
    override val managedAttributes = attributes ?: mapOf(
        BIAS_GENE to FloatAttribute(biasConfiguration),
        RESPONSE_GENE to FloatAttribute(responseConfiguration),
        ACTIVATION_GENE to ActivationAttribute(activationConfiguration),
        AGGREGATION_GENE to AggregationAttribute(aggregationConfiguration)
    )

    /**
     * # Computes the difference between another [NodeGene], [other].
     *
     * ### Distance is calculated as follows:
     *
     * 1. Add the absolute difference between the two gene's [BIAS_GENE] values. For example:
     * Gene A has a bias value of .2
     * Gene B has a bias value of .8
     *
     * The distance between these two values is .6; so that is the "distance" between these two values
     *
     * 2. Add the absolute difference between the two gene's [RESPONSE_GENE] values. For example:
     * Gene A has a response value of .3
     * Gene B has a response value of .6
     *
     * The distance between these two values is .3; so that is the "distance" between these two values
     *
     * 3. Add "1" if the activation functions differ
     * 4. Add "1" if the aggregation functions differ
     *
     * *Note*: If any other gene is passed in than a [NodeGene], you will get errors via the [getAs] function
     *
     * @return The absolute difference between biases + the absolute difference between responses +
     * 1 if the activations differ + 1 if the aggregation functions differ
     */
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
        /**
         * Used to index an attribute that manages the bias field of this gene
         */
        const val BIAS_GENE = "bias"

        /**
         * Used to index an attribute that manages the response field of this gene
         */
        const val RESPONSE_GENE = "response"

        /**
         * Used to index an attribute that manages the activation function of this gene
         */
        const val ACTIVATION_GENE = "activation"

        /**
         * Used to index an attribute that manages the aggregation function of this gene
         */
        const val AGGREGATION_GENE = "aggregation"
    }
}
