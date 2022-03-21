package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.configuration.GenomeConfiguration.FloatConfiguration
import com.david.lamar.kneat.util.MultiplierType
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class FloatAttribute(config: FloatConfiguration) : Attribute<Float, FloatConfiguration>(config) {

    private var min: Float = config.minValue ?: Float.MIN_VALUE
    private var max: Float = config.maxValue ?: Float.MAX_VALUE

    override var value: Float = calculateInitialValue()

    override fun mutate(): Float {
        val rand = Random.nextFloat()

        if (rand < config.mutationRate) {
            val gaussRand = java.util.Random().nextGaussian()
            val gauss = (gaussRand * config.mutatePower).toFloat()
            value = clamp(value + gauss)
        } else if (rand > config.replaceRate + config.mutationRate) {
            value = calculateInitialValue()
        }

        return value
    }

    private fun calculateInitialValue() : Float {
        val default = config.default
        val standardDev = config.initialStandardDeviation
        val meanValue = config.initialMean
        val type = config.multiplierType

        return default ?: getRandomValue(type = type, stdDev = standardDev, mean = meanValue)
    }

    private fun getRandomValue(type: MultiplierType, stdDev: Float, mean: Float) : Float {
        return when(type) {
            MultiplierType.GAUSSIAN, MultiplierType.NORMAL -> {
                val rand = java.util.Random().nextGaussian()
                clamp((rand * stdDev + mean).toFloat())
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

    private fun clamp(value: Float): Float {
        return max(min(value, max), min)
    }
}