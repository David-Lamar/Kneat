package kneat.evolution.species

import kneat.evolution.genome.Genome
import kneat.evolution.network.Aggregation
import kneat.evolution.stagnation.StagnationConfiguration

/**
 * A single "species" or group of genomes of similar genetic makeup.
 *
 * @property key The unique identifier of the species
 * @property members The individuals that make up the species
 * @property representative The single individual that acts as an example for the entire species
 * @property createdIn The generation that this species was created in
 * @property fitnessAggregationFunction The [Aggregation] function used to aggregate the fitness values for all individuals
 * in the species
 */
class Species(
    val key: Long,
    val members: List<Genome>,
    val representative: Genome,
    private val createdIn: Long,
    private val fitnessAggregationFunction: Aggregation
) {
    /**
     * The generation that this species last improved in
     */
    private var lastImprovedIn: Long = 0

    /**
     * The history of fitness [Aggregation]s this species has seen
     */
    private var fitnessHistory: MutableList<Float> = mutableListOf()

    /**
     * The current fitness of this species; calculated via the [fitnessAggregationFunction]
     */
    var currentFitness: Float = Float.MIN_VALUE
        private set

    /**
     * Whether or not this species is stagnant. Stagnation is defined via the [StagnationConfiguration] and
     * [kneat.evolution.stagnation.StagnationScheme]
     */
    var isStagnant: Boolean = false
        private set

    /**
     * Checks the current fitness value compared to the last known value in [fitnessHistory]. Based on the
     * [config], if the [StagnationConfiguration.improvementThreshold] hasn't been hit in
     * [StagnationConfiguration.maxStagnationGeneration] generations, the species will be marked as stagnant and be
     * considered for culling.
     */
    fun adjustStagnation(generation: Long, config: StagnationConfiguration) {
        val previousFitness = if (fitnessHistory.isEmpty()) {
            Float.MIN_VALUE
        } else {
            fitnessHistory.maxOf { it }
        }

        currentFitness = fitnessAggregationFunction.aggregate(getFitnessValues(generation).map { it.second })
        fitnessHistory.add(currentFitness)

        val desiredFitness = previousFitness + config.improvementThreshold

        if (currentFitness > desiredFitness) lastImprovedIn = generation

        isStagnant = lastImprovedIn >= config.maxStagnationGeneration
    }

    /**
     * Returns all of the fitness values for all of the individuals in the species; each fitness is indexed via
     * the [Genome.key] for the [Genome] which it's associated with
     */
    fun getFitnessValues(generation: Long) : List<Pair<Long, Float>> {
        return members.map { it.key to it.getFitness(generation) }
    }

    companion object {
        /**
         * Immutable function to return a new species object identical to the [Species] it's applied to, except for
         * the [representative] and [members] being the provided values.
         */
        fun Species.update(
            representative: Genome,
            members: List<Genome>
        ) : Species {
            return Species(
                key = this.key,
                members = members,
                representative = representative,
                createdIn = this.createdIn,
                fitnessAggregationFunction = this.fitnessAggregationFunction
            )
        }
    }
}



