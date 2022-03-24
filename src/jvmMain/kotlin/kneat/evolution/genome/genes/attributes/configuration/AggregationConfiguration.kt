package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.network.Aggregation
import kneat.evolution.network.Node

/**
 * Used to configure the [Aggregation] function used by a [Node] via its
 * [kneat.evolution.genome.genes.NodeGene].
 *
 * @property default see [AttributeConfiguration]. If null, a random value out of the [available] pool will be
 * selected
 * @property mutationRate see [AttributeConfiguration]
 * @property available The list of available [Aggregation] functions that we may pull from for this node to use
 * during mutation. Defaulted to all provided aggregation functions; see [Aggregation]
 */
data class AggregationConfiguration(
    override val default: Aggregation? = null,
    override val mutationRate: Float = 0f,
    val available: List<Aggregation> = listOf(
        Aggregation.Max, Aggregation.MaxAbsoluteValue, Aggregation.Mean, Aggregation.Median,
        Aggregation.Min, Aggregation.Product, Aggregation.Sum, Aggregation.Variance, Aggregation.StandardDeviation
    ),
) : AttributeConfiguration<Aggregation>()