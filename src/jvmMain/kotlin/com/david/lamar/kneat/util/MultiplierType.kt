package com.david.lamar.kneat.util

/**
 * Used primarily in a [com.david.lamar.kneat.genome.genes.attributes.FloatAttribute], determines whether
 * or not the initial value should be calculated via a Gausian / Normally distributed random number or
 * a uniformly distributed random number.
 *
 * [GAUSSIAN] and [NORMAL] are both the same; just different nomenclature
 */
enum class MultiplierType {
    GAUSSIAN, NORMAL, UNIFORM
}