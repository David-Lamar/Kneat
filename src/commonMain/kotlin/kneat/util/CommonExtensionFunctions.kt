package kneat.util

import kneat.PipelineState
import kneat.evolution.genome.Genome
import kneat.evolution.genome.genes.attributes.Attribute
import kneat.evolution.genome.genes.attributes.configuration.AttributeConfiguration
import kneat.util.reporting.Reporter
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * A helper function to report for an entire list of reporters all at once. It's not uncommon for
 * a developer to want reporters for different things; having to loop through every place we want to call
 * a reporting method is cumbersome. This simplifies that process
 */
fun List<Reporter>.report() : Reporter {
    val list = this

    return object : Reporter {
        override fun info(info: String) = list.forEach { it.info(info) }
        override fun warn(warning: String) = list.forEach { it.warn(warning) }
        override fun error(error: String, throwable: Throwable?) = list.forEach { it.error(error, throwable) }
        override fun onCreatingInitialPopulation() = list.forEach { it.onCreatingInitialPopulation() }
        override fun onSpeciating() = list.forEach { it.onSpeciating() }
        override fun onSpeciated() = list.forEach { it.onSpeciated()}
        override fun onEvaluating(genome: Genome) = list.forEach { it.onEvaluating(genome) }
        override fun onEvaluated() = list.forEach { it.onEvaluated() }
        override fun onGenerationStarted() = list.forEach { it.onGenerationStarted() }
        override fun onGenerationFinished() = list.forEach { it.onGenerationFinished()}
        override fun onCulling() = list.forEach { it.onCulling() }
        override fun onCulled() = list.forEach { it.onCulled() }
        override fun onReproducing() = list.forEach { it.onReproducing() }
        override fun onReproduced() = list.forEach { it.onReproduced()}
        override fun onExtinction() = list.forEach { it.onExtinction() }
        override fun onSolutionFound(fittest: Genome?) = list.forEach { it.onSolutionFound(fittest) }
        override fun onPaused(willResumeState: PipelineState) = list.forEach { it.onPaused(willResumeState) }
        override fun onSaving(wilLResumeState: PipelineState) = list.forEach { it.onSaving(wilLResumeState) }
        override fun onLoading() = list.forEach { it.onLoading() }
    }
}

/**
 * Removes a random item from the collection
 */
fun <E> MutableList<E>.removeRandom() {
    if (isNotEmpty()) remove(random())
}

/**
 * Adds all [items] to the collection
 */
fun <E> MutableList<E>.addAll(vararg items: E) {
    addAll(items)
}

/**
 * Helper function to retrieve an attribute from a map of attributes according to the indexed
 * name of the attribute being sought out.
 */
inline fun <reified T> Map<String, Attribute<out Any, out AttributeConfiguration<out Any>>>.getAs(key: String) : T {
    return this.getOrElse(key) { error("Invalid gene configuration. ") } as T
}


//TODO: Verify this is how the Java version works...
private var privateNextGaussian: Float? = null
fun Random.nextGaussian() : Float {
    privateNextGaussian?.let {
        privateNextGaussian = null
        return it
    }

    var first: Double
    var second: Double
    var s: Double
    do {
        first = 2 * nextDouble() - 1
        second = 2 * nextDouble() - 1

        s = first * first + second * second
    } while (s >= 1 || s == 0.0)

    val multiplier = sqrt(-2 * ln(s) / s)
    privateNextGaussian = (second * multiplier).toFloat()

    return (first * multiplier).toFloat()
}
