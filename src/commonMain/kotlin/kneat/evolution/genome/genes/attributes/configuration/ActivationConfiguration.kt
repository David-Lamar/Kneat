package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.network.Activation
import kneat.evolution.network.Node

/**
 * Used to configure the [Activation] function used by a [Node] via its
 * [kneat.evolution.genome.genes.NodeGene].
 *
 * @property default see [AttributeConfiguration]. If null, a random value out of the [available] pool will be
 * selected
 * @property mutationRate see [AttributeConfiguration]
 * @property available The list of available [Activation] functions that we may pull from for this node to use during
 * mutation. Defaulted to [Activation.Sigmoid] as it is a good, general purpose starting point
 */
data class ActivationConfiguration(
        override val default: Activation? = null,
        override val mutationRate: Float = 0f,
        val available: List<Activation> = listOf(Activation.Sigmoid)
) : AttributeConfiguration<Activation>()