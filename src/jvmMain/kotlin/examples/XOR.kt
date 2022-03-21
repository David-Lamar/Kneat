package examples

import com.david.lamar.kneat.Pipeline
import com.david.lamar.kneat.configuration.*
import com.david.lamar.kneat.genome.network.Activation
import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.genome.network.KneatNetwork
import com.david.lamar.kneat.util.reporting.StdOutReporter
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

val configuration = Configuration(
    terminationCriterion = TerminationCriterion(
        fitnessAggregationFunction = Aggregation.Max,
        fitnessThreshold = 3.9f
    ),
    genomeConfiguration = GenomeConfiguration(
        activationConfig = GenomeConfiguration.ActivationConfiguration(
            default = Activation.Sigmoid,
            available = listOf(Activation.Sigmoid),
            mutationRate = 0f
        ),
        aggregationConfiguration = GenomeConfiguration.AggregationConfiguration(
            default = Aggregation.Sum,
            available = listOf(Aggregation.Sum),
            mutationRate = 0f
        ),
        biasConfiguration = GenomeConfiguration.BiasConfiguration(
            initialMean = 0f,
            initialStandardDeviation = 1f,
            maxValue = 30f,
            minValue = -30f,
            mutatePower = .5f,
            mutationRate = .7f,
            replaceRate = .1f
        ),
        compatibilityConfiguration = GenomeConfiguration.CompatibilityConfiguration(
            disjointCoefficient = 1f,
            weightCoefficient = .5f,
            threshold = 3f
        ),
        connectionConfiguration = GenomeConfiguration.ConnectionConfiguration(
            initialConnection = GenomeConfiguration.ConnectionConfiguration.ConnectionType.FullSelection.All,
            allowRecurrence = false,
            additionProbability = .5f,
            deletionProbability = .5f,
            mutationRate = .001f,
            replaceRate = 0f
        ),
        nodeConfiguration = GenomeConfiguration.NodeConfiguration(
            inputs = 2,
            outputs = 1,
            additionProbability = .2f,
            deletionProbability = .2f
        ),
        responseConfiguration = GenomeConfiguration.ResponseConfiguration(
            initialMean = 1f,
            initialStandardDeviation = 0f,
            maxValue = 30f,
            minValue = -30f,
            mutatePower = 0f,
            mutationRate = 0f,
            replaceRate = 0f
        ),
        weightConfiguration = GenomeConfiguration.WeightConfiguration(
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
        maxStagnationGeneration = 20,
        fitnessAggregationFunction = Aggregation.Max
    ),
    resetOnExtinction = false
)

fun main() {
    runBlocking {
        val xOrInputs = listOf(listOf(0f, 0f), listOf(0f, 1f), listOf(1f, 0f), listOf(1f, 1f))
        val xOrOutputs = listOf(listOf(0f), listOf(1f), listOf(1f), listOf(0f))

        Pipeline
            .withConfiguration(configuration)
            .withReporter(StdOutReporter())
            .withEvaluationFunction { genome ->
                var fitness = 4f

                xOrInputs.zip(xOrOutputs).forEach {
                    val actualOutput = KneatNetwork.create(genome).activate(it.first, 1)
                    fitness -= (actualOutput[0] - it.second[0]).pow(2)
                }

                fitness
            }
            .start()
    }
}