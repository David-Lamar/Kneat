package com.david.lamar.kneat.evolution.reproduction

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.configuration.ReproductionConfiguration
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.genome.Genome
import com.david.lamar.kneat.util.reporting.Reporter

interface ReproductionScheme {
    fun create(
        config: GenomeConfiguration,
        amount: Int,
        reporters: MutableList<Reporter>
    ) : List<Genome>

    suspend fun reproduce(
        config: ReproductionConfiguration,
        populationSize: Int,
        species: List<Species>,
        generation: Long,
        reporters: MutableList<Reporter>
    ) : List<Genome>
}