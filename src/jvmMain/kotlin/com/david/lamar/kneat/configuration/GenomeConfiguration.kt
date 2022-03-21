package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.genome.network.Activation
import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.util.FloatRange
import com.david.lamar.kneat.util.MultiplierType

data class GenomeConfiguration(
    val activationConfig: ActivationConfiguration,
    val aggregationConfiguration: AggregationConfiguration,
    val biasConfiguration: BiasConfiguration,
    val responseConfiguration: ResponseConfiguration,
    val weightConfiguration: WeightConfiguration,
    val compatibilityConfiguration: CompatibilityConfiguration,
    val connectionConfiguration: ConnectionConfiguration,
    val nodeConfiguration: NodeConfiguration,
    val structureConfiguration: StructureConfiguration = StructureConfiguration()
) {
    abstract class AttributeConfiguration<T> {
        open val default: T? = null
        open val replaceRate: Float = 0f
        abstract val mutationRate: Float
    }

    data class ActivationConfiguration(
        override val default: Activation? = null, // None specified means random
        override val mutationRate: Float = 0f,
        val available: List<Activation> = listOf(
            Activation.AbsoluteValue, Activation.Clamped, Activation.Cubic, Activation.Exponential,
            Activation.ExponentialLinearUnit, Activation.Gaussian, Activation.Hat,
            Activation.HyperbolicTangent, Activation.Identity, Activation.Inverse,
            Activation.LeakyRectifiedLinearUnit, Activation.Logarithmic, Activation.RectifiedLinearUnit,
            Activation.ScaledExponentialLinearUNit, Activation.Sigmoid, Activation.Sin, Activation.Softplus,
            Activation.Square
        ),
    ) : AttributeConfiguration<Activation>()

    data class AggregationConfiguration(
        override val default: Aggregation? = null, // None specified means random
        override val mutationRate: Float = 0f,
        val available: List<Aggregation> = listOf(
            Aggregation.Max, Aggregation.MaxAbsoluteValue, Aggregation.Mean, Aggregation.Median,
            Aggregation.Min, Aggregation.Product, Aggregation.Sum, Aggregation.Variance, Aggregation.StandardDeviation
        ),
    ) : AttributeConfiguration<Aggregation>()

    abstract class BooleanConfiguration : AttributeConfiguration<Boolean>()

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

    data class ResponseConfiguration(
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

    data class CompatibilityConfiguration(
        val threshold: Float,
        val disjointCoefficient: Float,
        val weightCoefficient: Float
    )

    data class ConnectionConfiguration(
        override val default: Boolean? = null,
        override val replaceRate: Float, // Gets added to mutation rate when the value stays the same
        override val mutationRate: Float,
        val additionProbability: Float,
        val deletionProbability: Float,
        val allowRecurrence: Boolean,
        val initialConnection: ConnectionType = ConnectionType.Unconnected
    ) : BooleanConfiguration() {
        sealed class ConnectionType {
            object Unconnected : ConnectionType()

            sealed class SingleSelection : ConnectionType() {
                object HiddenOnly : SingleSelection()
                object OutputOnly : SingleSelection()
                object All : SingleSelection()
            }

            sealed class PartialSelection : ConnectionType() {
                @FloatRange(0.0, 1.0)
                abstract val probability: Float

                class HiddenOnly(override val probability: Float) : PartialSelection()
                class OutputOnly(override val probability: Float) : PartialSelection()
                class All(override val probability: Float) : PartialSelection()
            }

            sealed class FullSelection : ConnectionType() {
                object HiddenOnly : FullSelection()
                object OutputOnly : FullSelection()
                object All : FullSelection()
            }
        }
    }

    data class NodeConfiguration(
        val additionProbability: Float,
        val deletionProbability: Float,
        val initialHidden: Int = 0,
        val inputs: Int,
        val outputs: Int
    )

    data class StructureConfiguration(
        val allowedStructuralMutations: Int = Int.MAX_VALUE,
        val ensureStructuralMutation: Boolean = false
    )
}