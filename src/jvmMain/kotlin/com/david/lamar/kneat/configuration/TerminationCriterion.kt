package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.genome.network.Aggregation

/**
 * The criteria used to determine if a solution to the problem domain has been solved.
 *
 * @property fitnessAggregationFunction The aggregation function used to determine the overall fitness of a species
 * @property fitnessThreshold The value that the overall fitness (aggregated via [fitnessAggregationFunction]) needs
 * to be greater than or equal to in order to count as a valid solution
 */
//TODO: These values should be moved into the top level configuration since they're used multiple places and contribute to the pipeline itself
data class TerminationCriterion(
    val fitnessAggregationFunction: Aggregation = Aggregation.Mean, // TODO: This and StagnationConfiguration should be the same. Maybe move into a common place
    val fitnessThreshold: Float
)