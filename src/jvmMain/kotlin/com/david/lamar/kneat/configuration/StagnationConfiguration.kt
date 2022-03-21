package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.genome.network.Aggregation

data class StagnationConfiguration(
    val elitism: Int = 0,
    val fitnessAggregationFunction: Aggregation = Aggregation.Mean,
    val maxStagnationGeneration: Int = 15
)