package kneat.evolution.species

import kneat.evolution.genome.Genome
import kneat.evolution.network.Aggregation
import kneat.util.reporting.Reporter

/**
 * Separates a population of genomes into distinct species based on their genetic similarity.
 */
interface SpeciationScheme {

    /**
     * Creates a list of [Species] objects given the [population] and their genetic differences.
     *
     * @param compatibilityThreshold See [CompatibilityConfiguration]
     * @param population The population of [Genome]s that we're going to be speciating
     * @param generation The current generation being executed in the pipeline
     * @param fitnessAggregationFunction The [Aggregation] method used to combine fitness values across genomes
     * @param reporters Reporters to report on issues and information during a speciation event
     * @param existingSpecies The existing species, if any, to use as a head-start reference for speciation
     */
    suspend fun speciate(
        compatibilityThreshold: Float, // TODO: This should be passed in via a config
        population: List<Genome>,
        generation: Long,
        fitnessAggregationFunction: Aggregation, // TODO: This should be passed in via a config
        reporters: MutableList<Reporter>,
        existingSpecies: List<Species>? = null
    ) : List<Species>
}