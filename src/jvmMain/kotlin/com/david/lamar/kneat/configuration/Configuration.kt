package com.david.lamar.kneat.configuration

/**
 * TODO: Note that if terminationCriterion is set to null, foundSolution will need to be called to terminate the algorithm.
 *  Otherwise extinction will trigger an error
 */
data class Configuration(
    val terminationCriterion: TerminationCriterion?,
    val populationSize: Int,
    val resetOnExtinction: Boolean,
    val genomeConfiguration: GenomeConfiguration,
    val reproductionConfiguration: ReproductionConfiguration,
    val stagnationConfiguration: StagnationConfiguration
)