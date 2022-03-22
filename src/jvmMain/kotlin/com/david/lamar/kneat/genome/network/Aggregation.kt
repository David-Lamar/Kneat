package com.david.lamar.kneat.genome.network

import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A function used by a [Node] in a neural network to combine all of its inputs into one value to pass
 * into its activation function.
 */
interface Aggregation {

    /**
     * Aggregates a list of floats and returns a single value
     */
    fun aggregate(list: List<Float>) : Float

    /**
     * Returns all of the values of a list multiplied together
     */
    object Product : Aggregation {
        override fun aggregate(list: List<Float>) : Float {
            return list.reduce { accumulator, value ->
                accumulator * value
            }
        }
    }

    /**
     * Returns all of the values of a list summed together
     */
    object Sum : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                accumulator + value
            }
        }
    }

    /**
     * Returns the maximum value in the list of values
     */
    object Max : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                max(accumulator, value)
            }
        }
    }

    /**
     * Returns the minimum value in the list of values
     */
    object Min : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                min(accumulator, value)
            }
        }
    }

    /**
     * Returns the maximum value in the list of values where negatives are turned positive
     */
    object MaxAbsoluteValue : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                max(accumulator, abs(value))
            }
        }
    }

    /**
     * Returns the median value of the list
     */
    object Median : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            val sorted = list.sorted()
            return if (list.size % 2 == 0) {
                val half = sorted.size / 2
                (sorted[half] + sorted[half - 1]) / 2f
            } else {
                sorted[sorted.size / 2]
            }
        }
    }

    /**
     * Returns the mean, or average, of the list
     */
    object Mean : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.average().toFloat()
        }
    }

    /**
     * Returns the variance of the list of values
     */
    object Variance : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            val mean = Mean.aggregate(list)

            return list.map { value ->
                (value.toDouble() - mean).pow(2.0).toFloat()
            }.sum() / list.size
        }
    }

    /**
     * Returns the standard deviation of the list of values
     */
    object StandardDeviation : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return sqrt(Variance.aggregate(list))
        }
    }
}
