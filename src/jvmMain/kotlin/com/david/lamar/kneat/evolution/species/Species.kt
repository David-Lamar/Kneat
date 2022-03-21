package com.david.lamar.kneat.evolution.species

import com.david.lamar.kneat.configuration.StagnationConfiguration
import com.david.lamar.kneat.genome.Genome
import com.david.lamar.kneat.genome.network.Aggregation

class Species(
    val key: Long,
    val members: List<Genome>,
    val representative: Genome,
    private val createdIn: Long,
    private val fitnessAggregationFunction: Aggregation
) {
    private var lastImprovedIn: Long = 0
    private var fitnessHistory: MutableList<Float> = mutableListOf()

    var currentFitness: Float = Float.MIN_VALUE
        private set

    var isStagnant: Boolean = false

    fun adjustStagnation(generation: Long, config: StagnationConfiguration) {
        val previousFitness = if (fitnessHistory.isEmpty()) {
            Float.MIN_VALUE
        } else {
            fitnessHistory.maxOf { it }
        }

        currentFitness = fitnessAggregationFunction.aggregate(getFitnessValues(generation).map { it.second })
        fitnessHistory.add(currentFitness)

        if (currentFitness > previousFitness) lastImprovedIn = generation

        isStagnant = lastImprovedIn >= config.maxStagnationGeneration
    }

    fun getFitnessValues(generation: Long) : List<Pair<Long, Float>> {
        return members.map { it.key to it.getFitness(generation) }
    }

    companion object {
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



