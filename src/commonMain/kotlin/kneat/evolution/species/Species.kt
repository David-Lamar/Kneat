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
interface Species {
    val key: Long
    val members: List<Genome>
    val representative: Genome
    val createdIn: Long
    val fitnessAggregationFunction: Aggregation

    fun getCurrentFitness() : Float
    fun isStagnant() : Boolean
    fun adjustStagnation(generation: Long, config: StagnationConfiguration)
    fun getFitnessValues(generation: Long) : List<Pair<Long, Float>>

    fun update(representative: Genome, members: List<Genome>) : Species
}