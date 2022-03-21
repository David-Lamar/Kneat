package com.david.lamar.kneat.evolution.stagnation

import com.david.lamar.kneat.configuration.StagnationConfiguration
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.util.reporting.Reporter

interface StagnationScheme {
    suspend fun killOffStagnant(
        species: List<Species>,
        generation: Long,
        config: StagnationConfiguration,
        reporters: MutableList<Reporter>
    ) : List<Species>
}