package kneat.util

import kneat.evolution.genome.genes.Gene
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.lang.Integer.max
import kotlin.coroutines.coroutineContext

/**
 * Helper function that calculates the distance between two lists of [Gene]s.
 *
 * For more information about how distances are calculated, see the implementation of the
 * specific [Gene] classes used.
 */
inline fun <reified ID, reified GENE : Gene> List<Pair<ID, GENE>>.distance(
    to: List<Pair<ID, GENE>>,
    weightCoefficient: Float,
    disjointCoefficient: Float
) : Float {
    val otherElementMap = to.map { it.first to it.second }.toMap()
    var distance = 0f
    var disjoint = 0
    var accounted = 0

    forEach {
        val corresponding = otherElementMap[it.first]
        if (corresponding != null) {
            distance += it.second.distance(corresponding) * weightCoefficient
        } else {
            disjoint++
        }
        accounted++
    }

    if (accounted < to.size) disjoint += to.size - accounted
    distance += disjoint * disjointCoefficient
    distance /= max(size, to.size).toFloat()
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

