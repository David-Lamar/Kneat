package com.david.lamar.kneat.genome.network

import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

interface Aggregation {

    fun aggregate(list: List<Float>) : Float

    object Product : Aggregation {
        override fun aggregate(list: List<Float>) : Float {
            return list.reduce { accumulator, value ->
                accumulator * value
            }
        }
    }

    object Sum : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                accumulator + value
            }
        }
    }

    object Max : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                max(accumulator, value)
            }
        }
    }

    object Min : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                min(accumulator, value)
            }
        }
    }

    object MaxAbsoluteValue : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.reduce { accumulator, value ->
                max(accumulator, abs(value))
            }
        }
    }

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

    object Mean : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return list.average().toFloat()
        }
    }

    object Variance : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            val mean = Mean.aggregate(list)

            return list.map { value ->
                (value.toDouble() - mean).pow(2.0).toFloat()
            }.sum() / list.size
        }
    }

    object StandardDeviation : Aggregation {
        override fun aggregate(list: List<Float>): Float {
            return sqrt(Variance.aggregate(list))
        }
    }
}
