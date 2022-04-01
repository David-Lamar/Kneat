package kneat.evolution.reproduction

import kneat.evolution.genome.Genome
import kneat.evolution.genome.configuration.GenomeConfiguration
import kneat.evolution.species.Species
import kneat.util.reporting.Reporter

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
    ) : Map<Long, List<Genome>>

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
    ) : Map<Long, List<Genome>>
}