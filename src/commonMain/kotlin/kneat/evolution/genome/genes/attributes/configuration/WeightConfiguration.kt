package kneat.evolution.genome.genes.attributes.configuration

import kneat.util.MultiplierType

/**
 * Used to configure the Weight value used in a [Connection]. For more
 * information about how Weight affects a connection, see [Connection].
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
data class WeightConfiguration(
    override val default: Float? = null,
    override val mutationRate: Float,
    override val replaceRate: Float,
    override val initialMean: Float,
    override val initialStandardDeviation: Float,
    override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
    override val maxValue: Float? = null,
    override val minValue: Float? = null,
    override val mutatePower: Float
) : FloatConfiguration()