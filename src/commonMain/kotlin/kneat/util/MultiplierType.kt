package kneat.util

//import kneat.evolution.genome.genes.attributes.FloatAttribute
import kneat.util.MultiplierType.GAUSSIAN
import kneat.util.MultiplierType.NORMAL

/**
 * Used primarily in a [FloatAttribute], determines whether or not the initial value should be calculated
 * via a Gaussian / Normally distributed random number or a uniformly distributed random number.
 *
 * [GAUSSIAN] and [NORMAL] are both the same; just different nomenclature
 */
enum class MultiplierType {
    GAUSSIAN, NORMAL, UNIFORM
}