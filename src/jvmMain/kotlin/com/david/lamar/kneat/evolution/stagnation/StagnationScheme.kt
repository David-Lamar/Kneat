package com.david.lamar.kneat.evolution.stagnation

import com.david.lamar.kneat.configuration.StagnationConfiguration
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.util.reporting.Reporter

/**
 * Method in which a population is culled based on the parameters set in [StagnationConfiguration]
 */
interface StagnationScheme {

    /**
     * Removes stagnant species from the overall population to encourage better quality genomes.
     *
     * @param species The list of species we're investigating for stagnation
     * @param generation The current generation; used to determine the length of time a species has gone without improving
     * @param config The specification used to determine how the population should be culled
     * @param reporters Reporters to report on issues and information during a culling event
     *
     * @return The new list of species minus any that have been stagnant
     */
    suspend fun killOffStagnant(
        species: List<Species>,
        generation: Long,
        config: StagnationConfiguration,
        reporters: MutableList<Reporter>
    ) : List<Species>
}