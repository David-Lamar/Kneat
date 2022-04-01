package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.genome.genes.attributes.Attribute
import kneat.util.MultiplierType

/**
 * Used to configure a [Float] value controlled by a [kneat.evolution.genome.genes.Gene]. Primary use cases
 * are for "Bias" and "Response" for a [Node] as well as the "Weight"
 * for a [Connection]
 *
 * @property default see [AttributeConfiguration]; if null, will be a random value calculated via [initialMean],
 * [initialStandardDeviation], and [multiplierType]. See [multiplierType] for more information about this
 * calculation.
 * @property mutationRate see [AttributeConfiguration]
 * @property replaceRate see [AttributeConfiguration]
 * @property mutatePower During a mutation, this value is multiplied by the zero-centered normal distribution and
 * added to the existing value of an [Attribute]. This is essentially
 * how drastic of a change will happen to the attribute value during a mutation.
 * @property initialMean If a [default] isn't specified, will be used to calculate the initial value; see
 * [multiplierType] for more details about this calculation.
 * @property initialStandardDeviation If a [default] isn't specified, will be used to calculate the initial value; see
 * [multiplierType] for more details about this calculation.
 * @property multiplierType If a [default] isn't specified, when this value is [MultiplierType.GAUSSIAN] or
 * [MultiplierType.NORMAL] the attribute's initial value will be a random value established via a
 * normal (gaussian) distribution zero-centered around the [initialStandardDeviation] and [initialMean]. When this
 * value is [MultiplierType.UNIFORM], the attribute's initial value will be calculated from a uniformly
 * distributed random value between [minValue] and [maxValue]. In all cases, the resolved value will be constrained
 * between [minValue] and [maxValue] if defined.
 * @property minValue The minimum allowed value of this attribute. When null, a minimum value of [Float.MIN_VALUE]
 * will be used.
 * @property maxValue The maximum allowed value of this attribute. When null, a maximum value of [Float.MAX_VALUE]
 * will be used.
 */
abstract class FloatConfiguration : AttributeConfiguration<Float>() {
    abstract override val default: Float?
    abstract override val mutationRate: Float
    abstract override val replaceRate: Float
    abstract val mutatePower: Float
    abstract val initialMean: Float
    abstract val initialStandardDeviation: Float
    abstract val multiplierType: MultiplierType
    abstract val maxValue: Float?
    abstract val minValue: Float?
}