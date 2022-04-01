package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.network.Aggregation

/**
 * Used to configure the [Aggregation] function used by a [Node] via its
 * [kneat.evolution.genome.genes.NodeGene].
 *
 * @property default see [AttributeConfiguration]. If null, a random value out of the [available] pool will be
 * selected
 * @property mutationRate see [AttributeConfiguration]
 * @property available The list of available [Aggregation] functions that we may pull from for this node to use
 * during mutation. Defaulted to [Aggregation.Sum] as it is a good, general purpose starting point
 */
data class AggregationConfiguration(
    override val default: Aggregation? = Aggregation.Sum,
    override val mutationRate: Float = 0f,
    val available: List<Aggregation> = listOf(Aggregation.Sum)
) : AttributeConfiguration<Aggregation>()