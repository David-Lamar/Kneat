package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.genome.network.Aggregation

data class TerminationCriterion(
    val fitnessAggregationFunction: Aggregation = Aggregation.Mean,
    val fitnessThreshold: Float
)