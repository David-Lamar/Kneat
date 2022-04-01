package examples

import kneat.Pipeline
import kneat.evolution.configuration.Configuration
import kneat.evolution.configuration.TerminationCriterion
import kneat.evolution.genome.configuration.CompatibilityConfiguration
import kneat.evolution.genome.configuration.GenomeConfiguration
import kneat.evolution.genome.genes.attributes.configuration.BiasConfiguration
import kneat.evolution.genome.genes.attributes.configuration.WeightConfiguration
import kneat.evolution.genome.genes.configuration.ConnectionConfiguration
import kneat.evolution.genome.genes.configuration.ConnectionType
import kneat.evolution.genome.genes.configuration.NodeConfiguration
import kneat.evolution.network.Aggregation
import kneat.evolution.network.KneatNetwork
import kneat.evolution.reproduction.ReproductionConfiguration
import kneat.evolution.stagnation.StagnationConfiguration
import kneat.util.reporting.StdOutReporter
import kotlinx.coroutines.runBlocking
import kotlin.math.pow
import kotlin.time.ExperimentalTime

val xorConfiguration = Configuration(
    terminationCriterion = TerminationCriterion(
        fitnessThreshold = 3.9f
    ),
    genomeConfiguration = GenomeConfiguration(
        biasConfiguration = BiasConfiguration(
            initialMean = 0f,
            initialStandardDeviation = 1f,
            maxValue = 30f,
            minValue = -30f,
            mutatePower = .5f,
            mutationRate = .7f,
            replaceRate = .1f
        ),
        compatibilityConfiguration = CompatibilityConfiguration(
            disjointCoefficient = 1f,
            weightCoefficient = .5f,
            threshold = 2f
        ),
        connectionConfiguration = ConnectionConfiguration(
            default = true,
            initialConnection = ConnectionType.FullSelection.All,
            mutationRate = .01f,
            replaceRate = 0f,
            additionProbability = .5f,
            deletionProbability = .5f,
            allowRecurrence = false
        ),
        nodeConfiguration = NodeConfiguration(
            inputs = 2,
            outputs = 1,
            additionProbability = .2f,
            deletionProbability = .2f
        ),
        weightConfiguration = WeightConfiguration(
            initialMean = 0f,
            initialStandardDeviation = 1f,
            maxValue = 30f,
            minValue = -30f,
            mutatePower = .5f,
            mutationRate = .8f,
            replaceRate = .1f
        )
    ),
    populationSize = 150,
    reproductionConfiguration = ReproductionConfiguration(
        elitism = 2,
        survivalThreshold = .2f
    ),
    stagnationConfiguration = StagnationConfiguration(
        elitism = 2,
        maxStagnationGeneration = 5,
        fitnessAggregationFunction = Aggregation.Max,
        improvementThreshold = 0f
    ),
    resetOnExtinction = false
)

@ExperimentalTime
fun main() {
    runBlocking {
        val xOrInputs = listOf(listOf(0f, 0f), listOf(0f, 1f), listOf(1f, 0f), listOf(1f, 1f))
        val xOrOutputs = listOf(listOf(0f), listOf(1f), listOf(1f), listOf(0f))

        Pipeline
            .withConfiguration(xorConfiguration)
            .withReporter(StdOutReporter())
            .withEvaluationFunction { genome ->
                var fitness = 4f
                val network = KneatNetwork.create(genome)

                xOrInputs.zip(xOrOutputs).forEach {
                    val actualOutput = network.activate(it.first, 1)
                    fitness -= (actualOutput[0] - it.second[0]).pow(2)
                }

                (network as KneatNetwork).clean() //TODO: make this not bad

                fitness
            }
            .start()
    }
}