package com.david.lamar.kneat.evolution.reproduction

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.configuration.ReproductionConfiguration
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.genome.Genome
import com.david.lamar.kneat.util.reporting.Reporter

/**
 * The method in which genomes will reproduce to create the next generation of individuals.
 */
interface ReproductionScheme {

    /**
     * Creates an initial population of [amount] [Genome]s with the [config] and [reporters] specified
     */
    fun create(
        config: GenomeConfiguration,
        amount: Int,
        reporters: MutableList<Reporter>
    ) : List<Genome>

    /**
     * Reproduces a population with an amount of individuals up to the [populationSize] given the existing [species].
     *
     * @param config The [ReproductionConfiguration] used to determine the behavior of reproduction
     * @param populationSize The size the new population should be
     * @param species The existing species currently present in the pipeline
     * @param generation The generation we're currently in
     * @param reporters Reporters to report on issues and information during a reproduction event
     */
    suspend fun reproduce(
        config: ReproductionConfiguration,
        populationSize: Int,
        species: List<Species>,
        generation: Long,
        reporters: MutableList<Reporter>
    ) : List<Genome>
}