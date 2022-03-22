package com.david.lamar.kneat.evolution.stagnation

import com.david.lamar.kneat.configuration.StagnationConfiguration
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.util.SPECIES_EXTINCTION_MESSAGE
import com.david.lamar.kneat.util.report
import com.david.lamar.kneat.util.reporting.Reporter

class KneatStagnationScheme : StagnationScheme {
    override suspend fun killOffStagnant(
        species: List<Species>,
        generation: Long,
        config: StagnationConfiguration,
        reporters: MutableList<Reporter>
    ) : List<Species> {
        species.forEach { it.adjustStagnation(generation, config) }

        val groupedStagnant = species
            .sortedBy { it.currentFitness }
            .groupBy { it.isStagnant }

        val stagnant = groupedStagnant.getOrDefault(false, emptyList())
        val kept = stagnant.takeLast(config.elitism)

        for (dead in stagnant.filter { !kept.contains(it) }) {
            reporters.report().info(SPECIES_EXTINCTION_MESSAGE.format(dead.key))
        }

        return groupedStagnant.getOrDefault(true, emptyList()) + kept
    }
}

