package kneat.evolution.reproduction

import kneat.evolution.genome.Genome
import kneat.evolution.genome.KneatGenome
import kneat.evolution.genome.configuration.GenomeConfiguration
import kneat.evolution.network.Aggregation
import kneat.evolution.species.Species
import kneat.util.report
import kneat.util.reporting.Reporter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

open class KneatReproductionScheme : ReproductionScheme {

    private val ancestors: MutableMap<Long, Pair<Long, Long>> = mutableMapOf()

    override fun create(
        config: GenomeConfiguration,
        amount: Int,
        reporters: MutableList<Reporter>
    ) : List<KneatGenome> {
        return (1L..amount).map {
            KneatGenome(
                id = it,
                config = config,
                specifiedReporters = reporters
            )
        }
    }

    //TODO: Expensive operations -- Parallelize
    override suspend fun reproduce(
        config: ReproductionConfiguration,
        populationSize: Int,
        species: List<Species>,
        generation: Long,
        reporters: MutableList<Reporter>
    ) : List<Genome> {
        val allFitness = species.flatMap { it.getFitnessValues(generation).map { value -> value.second } }
        val minFitness: Float = allFitness.minOf { it }
        val maxFitness: Float = allFitness.maxOf { it }
        val fitnessRange = max(1f, maxFitness - minFitness)

        val adjustedFitnesses = mutableMapOf<Long, Float>()

        species.forEach {
            val meanFitness = Aggregation.Mean.aggregate(it.getFitnessValues(generation).map { value -> value.second })
            val adjustedFitness = (meanFitness - minFitness) / fitnessRange
            adjustedFitnesses[it.key] = adjustedFitness
        }

        val averageAdjustedFitness = Aggregation.Mean.aggregate(adjustedFitnesses.values.toList())
        reporters.report().info("Average adjusted fitness: $averageAdjustedFitness")

        val previousSizes = species.map { it.members.size }
        val minSize = config.minSpeciesSize
        val minSpeciesSize = max(minSize, config.elitism)
        val spawnAmounts = computeSpawn(
            config = config,
            adjustedFitness = adjustedFitnesses.values.toList(),
            minimumSpeciesSize = minSpeciesSize,
            populationSize = populationSize,
            previousSizes = previousSizes
        )

        val newPopulation: MutableList<Genome> = mutableListOf()

        species.zip(spawnAmounts).forEach { (spec, spawn) ->
            var toSpawn = max(spawn, config.elitism)
            val members = spec.members.sortedBy { it.getFitness(generation) }

            val elites = members.takeLast(config.elitism)
            toSpawn -= elites.size
            newPopulation.addAll(elites)

            if (toSpawn > 0) {
                val reproductionCutoff = max(
                    ceil(config.survivalThreshold * members.size).toInt(),
                    2
                )

                val fittest = members.takeLast(reproductionCutoff)
                var genomeIndex = newPopulation.maxOf { it.id } + 1
                for (i in 1..spawn) {
                    val parent1 = fittest.random()
                    val parent2 = fittest.random()

                    val newId = genomeIndex++
                    val child = KneatGenome.createFromCrossover(
                        newId,
                        parent1,
                        parent2
                    )
                    child.mutate()

                    newPopulation.add(child)
                    ancestors[newId] = Pair(parent1.id, parent2.id)
                }
            }
        }

        return newPopulation
    }

    companion object {
        fun computeSpawn(
            config: ReproductionConfiguration,
            adjustedFitness: List<Float>,
            previousSizes: List<Int>,
            populationSize: Int,
            minimumSpeciesSize: Int
        ) : List<Int> {
            val adjustedFitnessSum = adjustedFitness.sum()
            val spawnAmounts = mutableListOf<Int>()

            adjustedFitness.zip(previousSizes).forEach { (fitness, size) ->
                val newSize = if (adjustedFitnessSum > 0) {
                    max(minimumSpeciesSize.toFloat(), fitness / adjustedFitnessSum * populationSize)
                } else {
                    config.minSpeciesSize.toFloat()
                }

                val d = (newSize - size) * .5f
                val c = d.roundToInt()

                val spawn = when {
                    abs(c) > 0 -> { size + c }
                    d > 0 -> { size + 1 }
                    else -> { size - 1 }
                }

                spawnAmounts.add(spawn)
            }

            val totalSpawn = spawnAmounts.sum()
            val normalize = populationSize / totalSpawn

            return spawnAmounts.map {
                max(config.minSpeciesSize, it * normalize)
            }
        }
    }
}