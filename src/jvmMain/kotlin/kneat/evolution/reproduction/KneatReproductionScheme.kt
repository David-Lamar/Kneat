package kneat.evolution.reproduction

import kneat.evolution.genome.Genome
import kneat.evolution.genome.KneatGenome
import kneat.evolution.genome.configuration.GenomeConfiguration
import kneat.evolution.network.Aggregation
import kneat.evolution.species.Species
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
    ) : Map<Long, List<Genome>> {
        val list = (1L..amount).map {
            val genome = KneatGenome(
                id = it,
                config = config,
                specifiedReporters = reporters
            )

            genome.mutate()
            genome.mutate()

            genome
        }

        return mapOf(0L to list)
    }

    //TODO: Expensive operations -- Parallelize
    override suspend fun reproduce(
        config: ReproductionConfiguration,
        populationSize: Int,
        species: List<Species>,
        generation: Long,
        reporters: MutableList<Reporter>
    ) : Map<Long, List<Genome>> {
        var minFitness: Float = Float.MAX_VALUE
        var maxFitness: Float = Float.MIN_VALUE
        val meanFitnessMap = mutableMapOf<Long, Float>()
        val previousSizes = mutableMapOf<Long, Int>()
        val memberMap = mutableMapOf<Long, List<Genome>>()
        var maxGenomeId: Long = 0

        species.forEach { spec ->
            previousSizes[spec.key] = spec.members.size
            memberMap[spec.key] = spec.members

            val currentValues = mutableListOf<Float>()
            spec.getFitnessValues(generation).forEach { fit ->
                val fitness = fit.second
                if (minFitness > fitness) minFitness = fitness
                if (maxFitness < fitness) maxFitness = fitness
                if (maxGenomeId < fit.first) maxGenomeId = fit.first
                currentValues.add(fitness)
            }
            val meanFitness = Aggregation.Mean.aggregate(currentValues)
            meanFitnessMap[spec.key] = meanFitness
        }

        val fitnessRange = max(1f, maxFitness - minFitness)
        val adjustedFitnessMap = meanFitnessMap.map {
            it.key to (it.value - minFitness) / fitnessRange
        }.toMap()

        val minSize = config.minSpeciesSize
        val minSpeciesSize = max(minSize, config.elitism)
        val spawnAmounts = computeSpawn(
            config = config,
            adjustedFitness = adjustedFitnessMap,
            minimumSpeciesSize = minSpeciesSize,
            populationSize = populationSize,
            previousSizes = previousSizes
        )

        val newPopulation = mutableMapOf<Long, List<Genome>>()
        var genomeIndex = maxGenomeId + 1

        spawnAmounts.forEach { (key, spawnAmount) ->
            var toSpawn = max(spawnAmount, config.elitism)
            val members = (memberMap[key] ?: error("")).sortedBy { it.getFitness(generation) }
            val elites = members.takeLast(config.elitism)
            toSpawn -= elites.size

            newPopulation[key] = (newPopulation[key] ?: emptyList()) + elites

            if (toSpawn > 0) {
                val reproductionCutoff = max(
                    ceil(config.survivalThreshold * members.size).toInt(),
                    2
                )

                val fittest = members.takeLast(reproductionCutoff)
                for (i in 1..toSpawn) {
                    val parent1 = fittest.random()
                    val parent2 = fittest.random()

                    val newId = genomeIndex++
                    val child = KneatGenome.createFromCrossover(
                        newId,
                        parent1,
                        parent2
                    )
                    child.mutate()

                    newPopulation[key] = (newPopulation[key] ?: emptyList()) + child

                    ancestors[newId] = Pair(parent1.id, parent2.id)
                }
            }
        }

        return newPopulation
    }

    companion object {
        fun computeSpawn(
            config: ReproductionConfiguration,
            adjustedFitness: Map<Long, Float>,
            previousSizes: Map<Long, Int>,
            populationSize: Int,
            minimumSpeciesSize: Int
        ) : Map<Long, Int> {
            val adjustedFitnessSum = adjustedFitness.values.sum()
            val spawnAmounts = mutableMapOf<Long, Int>()
            var totalSpawn = 0

            adjustedFitness.forEach { (key, fitness) ->
                val size = previousSizes[key] ?: error("")
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

                totalSpawn += spawn
                spawnAmounts[key] = spawn
            }

            val normalize = populationSize / totalSpawn

            return spawnAmounts.mapValues { max(minimumSpeciesSize, it.value * normalize) }
        }
    }
}