package com.david.lamar.kneat.evolution.species

import com.david.lamar.kneat.genome.Genome
import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.util.reporting.Reporter

interface SpeciationScheme {
    suspend fun speciate(
        compatibilityThreshold: Float,
        population: List<Genome>,
        generation: Long,
        fitnessAggregationFunction: Aggregation,
        reporters: MutableList<Reporter>,
        existingSpecies: List<Species>? = null
    ) : List<Species>
}