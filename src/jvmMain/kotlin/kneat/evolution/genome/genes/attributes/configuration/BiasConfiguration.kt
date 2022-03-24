package kneat.evolution.genome.genes.attributes.configuration

import kneat.util.MultiplierType
import kneat.evolution.network.Node

/**
 * Used to configure the Bias value used in a [Node]. For more information
 * about how Bias affects a node, see [Node].
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
data class BiasConfiguration(
    override val default: Float? = null,
    override val replaceRate: Float,
    override val mutationRate: Float,
    override val initialMean: Float,
    override val initialStandardDeviation: Float,
    override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
    override val maxValue: Float? = null,
    override val minValue: Float? = null,
    override val mutatePower: Float
) : FloatConfiguration()