package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.network.Node
import kneat.util.MultiplierType

/**
 * Used to configure the Response value used in a [Node]. For more information
 * about how Response affects a node, see [Node].
 *
 * Note: The initial values for this are set to not affect a node. Response isn't a value needed
 * on a node to make an efficient and dense network, but it may help. We've defaulted it to
 * be non-effective but allow for extension for cases where it may be useful.
 *
 * @property default see [AttributeConfiguration]
 * @property mutationRate see [AttributeConfiguration]
 * @property replaceRate see [AttributeConfiguration]
 * @property mutatePower see [FloatConfiguration]
 * @property initialMean see [FloatConfiguration]
 * @property initialStandardDeviation see [FloatConfiguration]
 * @property multiplierType see [FloatConfiguration]
 * @property maxValue see [FloatConfiguration]
 * @property minValue see [FloatConfiguration]
 */
data class ResponseConfiguration(
    override val default: Float? = null,
    override val mutationRate: Float = 0f,
    override val replaceRate: Float = 0f,
    override val initialMean: Float = 1f,
    override val initialStandardDeviation: Float = 0f,
    override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
    override val maxValue: Float? = null,
    override val minValue: Float? = null,
    override val mutatePower: Float = 0f
) : FloatConfiguration()