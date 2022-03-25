package kneat.evolution.genome.genes.attributes

import kneat.evolution.genome.genes.ConnectionGene
import kneat.evolution.genome.genes.NodeGene
import kneat.evolution.genome.genes.attributes.configuration.FloatConfiguration
import kneat.util.MultiplierType
import kneat.util.nextGaussian
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A custom [Attribute] that manages a float value; primarily used within [ConnectionGene] and
 * [NodeGene] to manage different modifiers associated with the action potential of a neuron.
 * May be configured via a [FloatConfiguration].
 *
 * @param config The [FloatConfiguration] used to configure this attribute
 */
class FloatAttribute(config: FloatConfiguration) : Attribute<Float, FloatConfiguration>(config) {

    /**
     * The minimum value this attribute can contain; defaults to [Float.MIN_VALUE]
     */
    private var min: Float = config.minValue ?: Float.MIN_VALUE

    /**
     * The maximum value this attribute can contain; defaults to [Float.MAX_VALUE]
     */
    private var max: Float = config.maxValue ?: Float.MAX_VALUE

    override var value: Float = calculateInitialValue()

    override fun mutate(): Float {
        val rand = Random.nextFloat()

        if (rand < config.mutationRate) {
            val gauss = (Random.nextGaussian() * config.mutatePower)
            value = clamp(value + gauss)
        } else if (rand > config.replaceRate + config.mutationRate) {
            value = calculateInitialValue()
        }

        return value
    }

    /**
     * Calculates the initial value this attribute should have based on the [config] values provided
     */
    private fun calculateInitialValue() : Float {
        val default = config.default
        val standardDev = config.initialStandardDeviation
        val meanValue = config.initialMean
        val type = config.multiplierType

        return default ?: getRandomValue(type = type, stdDev = standardDev, mean = meanValue)
    }

    /**
     * Generates a random number using one of the different [MultiplierType] provided via [type]
     */
    private fun getRandomValue(type: MultiplierType, stdDev: Float, mean: Float) : Float {
        return when(type) {
            MultiplierType.GAUSSIAN, MultiplierType.NORMAL -> {
                clamp((Random.nextGaussian() * stdDev + mean))
            }
            MultiplierType.UNIFORM -> {
                val uniMin = mean - (2f * stdDev)
                val uniMax = mean + (2f * stdDev)
                this.min = max(this.min, uniMin)
                this.max = min(this.max, uniMax)
                this.min + Random.nextFloat() * (this.max - this.min)
            }
        }
    }

    /**
     * Normalizes the value of this attribute between its [min] and [max]
     */
    private fun clamp(value: Float): Float {
        return max(min(value, max), min)
    }
}