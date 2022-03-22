package com.david.lamar.kneat.util

import com.david.lamar.kneat.genome.genes.Gene
import com.david.lamar.kneat.genome.network.NetworkElement
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

    for (item in this@sortedParallelBy) {
        newSelector[item] = async { selector(item) }
    }

    val selectors = newSelector.map { (item, wait) ->
        item to wait.await()
    }.toMap()


    return@withContext sortedBy { selectors[it] }
}