package kneat.evolution.reproduction

/**
 * Used to configure how a [ReproductionScheme] behaves.
 *
 * @property elitism The number of "elite" (fittest) members of a species that will stay in the population
 * during the next generation
 * @property survivalThreshold The percentage of a species population to use for reproduction; the fittest members will
 * be saved first while the others will "die out"
 * @property minSpeciesSize The minimum number of members a species is allowed to have after a reproduction event
 */
data class ReproductionConfiguration(
    val elitism: Int = 0,
    val survivalThreshold: Float = .2f,
    val minSpeciesSize: Int = 2
)