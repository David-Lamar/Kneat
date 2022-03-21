package com.david.lamar.kneat.util

import com.david.lamar.kneat.genome.genes.Gene
import com.david.lamar.kneat.genome.network.NetworkElement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

fun <E> MutableList<E>.removeRandom() {
    if (isNotEmpty()) remove(random())
}

fun <E> MutableList<E>.addAll(vararg items: E) {
    addAll(items)
}

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

    val disjointNodes = (size - equalCount) + (to.size - equalCount)
    distance += disjointNodes * disjointCoefficient
    distance /= Integer.max(size, to.size).toFloat()

    return distance
}

/**
 * Should be used when the mapping function is expensive and potentially numerous. Using this is not
 * justified in cases where the transform function is simple / quick as the overhead doesn't justify it
 */
suspend inline fun <T, R> Iterable<T>.mapParallel(crossinline transform: suspend (T) -> R) = withContext(coroutineContext) {
    val list = mutableListOf<Deferred<R>>()

    for (item in this@mapParallel) {
        list.add(async { transform(item) })
    }

    list.map { it.await() }
}

//public inline fun <T> Iterable<T>.parallelSortedBy(crossinline selector: (T) -> R?): List<T> {
//    if (this is Collection) {
//        if (size <= 1) return this.toList()
//        @Suppress("UNCHECKED_CAST")
//        return (toTypedArray<Any?>() as Array<T>).apply { sortWith(comparator) }.asList()
//    }
//    return toMutableList().apply { sortWith(comparator) }
//}

suspend inline fun <T, R : Comparable<R>> Collection<T>.sortedParallelBy(crossinline selector: suspend (T) -> R?) = withContext(coroutineContext) {
    if (size <= 1) return@withContext toList()

    val newSelector = mutableMapOf<T, Deferred<R?>>()

    for (item in this@sortedParallelBy) {
        newSelector[item] = async { selector(item) }
    }

    val selectors = newSelector.map { (item, wait) ->
        item to wait.await()
    }.toMap()


    return@withContext sortedBy { selectors[it] }
}