package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.genome.network.Aggregation

/**
 * Used to configure how a [com.david.lamar.kneat.evolution.stagnation.StagnationScheme] behaves.
 *
 * @property elitism The number of "elite" (fittest) members of a species that will not be affected by culling
 * due to stagnation
 * @property improvementThreshold The threshold required to consider a species as "improved" in regards to its last
 * generation; i.e. The species's fitness (aggregated by [fitnessAggregationFunction]) will need to improve by
 * [improvementThreshold] before it's not considered stagnant. Defaulted to 0 to capture any improvement
 * @property fitnessAggregationFunction The aggregation function used to aggregate the species' member's fitness. This
 * value will be used to compare whether or not a species has "improved" since the last generation.
 * @property maxStagnationGeneration The number of generations that are allowed to pass before culling via stagnation
 * begins. If a species has not improved in this number of generations, it will completely die out (except for the
 * amount of individuals specified via [elitism], if any)
 */
data class StagnationConfiguration(
    val elitism: Int = 0,
    val improvementThreshold: Float = 0f, //TODO: This isn't currently being used; it should be
    val fitnessAggregationFunction: Aggregation = Aggregation.Mean,
    val maxStagnationGeneration: Int = 15
)