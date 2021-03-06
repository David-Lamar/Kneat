package com.david.lamar.kneat.configuration

/**
 * The primary configuration class used to provide a [com.david.lamar.kneat.Pipeline] with the necessary information
 * it needs to evolve neural networks.
 *
 * @property terminationCriterion The criteria used to determine if a solution has been found or not. If null, the
 * pipeline will continue indefinitely (except in the case of an extinction event where [resetOnExtinction] is not
 * enabled).
 * @property populationSize The amount of individuals (genomes) that will be present in each generation
 * @property resetOnExtinction Resets the pipeline and starts fresh if an extinction even occurs
 * @property genomeConfiguration [com.david.lamar.kneat.genome.Genome] specific configuration. See [GenomeConfiguration]
 * @property reproductionConfiguration [com.david.lamar.kneat.evolution.reproduction.ReproductionScheme] specific
 * configuration. See [ReproductionConfiguration]
 * @property stagnationConfiguration [com.david.lamar.kneat.evolution.stagnation.StagnationScheme] specific
 * configuration. See [StagnationConfiguration]
 */
data class Configuration(
    val terminationCriterion: TerminationCriterion?, //TODO: This value currently only handles being present gracefully. If not defined, it may lead to errors
    val populationSize: Int,
    val resetOnExtinction: Boolean, // TODO: This value is currently unused and should be
    val genomeConfiguration: GenomeConfiguration,
    val reproductionConfiguration: ReproductionConfiguration,
    val stagnationConfiguration: StagnationConfiguration
)