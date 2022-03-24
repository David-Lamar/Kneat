package kneat.util

import kneat.Pipeline
import kneat.evolution.genome.Genome
import kneat.evolution.genome.genes.Gene
import kneat.evolution.genome.genes.attributes.Attribute
import kneat.evolution.genome.genes.attributes.configuration.AttributeConfiguration
import kneat.evolution.network.NetworkElement
import kneat.util.reporting.Reporter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

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
 * Helper function that calculates the distance between two lists of [NetworkElement]s.
 *
 * For more information about how distances are calculated, see the implementation of the
 * specific [Gene] classes used.
 */
inline fun <reified ID, reified GENE : Gene> List<NetworkElement<ID, GENE>>.distance(
    to: List<NetworkElement<ID, GENE>>,
    weightCoefficient: Float,
    disjointCoefficient: Float,
) : Float {
    var distance = 0f
    var equalCount = 0

    forEach { thisElement ->
        to.forEach inner@ { otherElement ->
            if (thisElement.id == otherElement.id) {
                equalCount++
                distance += thisElement.gene.distance(otherElement.gene) * weightCoefficient
                return@inner
            }
        }
    }

    val disjointElements = (size - equalCount) + (to.size - equalCount)
    distance += disjointElements * disjointCoefficient
    distance /= Integer.max(size, to.size).toFloat()

    return distance
}

/**
 * [Iterable.map] but asynchronous
 *
 * Should be used when the mapping function is expensive and potentially numerous. Using this is not
 * justified in cases where the transform function is simple / quick as the overhead of creating these
 * async jobs isn't justified for such situations.
 */
suspend inline fun <T, R> Iterable<T>.mapParallel(crossinline transform: suspend (T) -> R) = withContext(coroutineContext) {
    val list = mutableListOf<Deferred<R>>()

    for (item in this@mapParallel) {
        list.add(async { transform(item) })
    }

    list.map { it.await() }
}

/**
 * Helper function to retrieve an attribute from a map of attributes according to the indexed
 * name of the attribute being sought out.
 */
inline fun <reified T> Map<String, Attribute<out Any, out AttributeConfiguration<out Any>>>.getAs(key: String) : T {
    return this.getOrElse(key) { error("Invalid gene configuration. ") } as T
}

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
        override fun onPaused(willResumeState: Pipeline.PipelineState) = list.forEach { it.onPaused(willResumeState) }
        override fun onSaving(wilLResumeState: Pipeline.PipelineState) = list.forEach { it.onSaving(wilLResumeState) }
        override fun onLoading() = list.forEach { it.onLoading() }
    }
}