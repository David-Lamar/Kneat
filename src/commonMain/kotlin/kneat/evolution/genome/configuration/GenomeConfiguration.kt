package kneat.evolution.genome.configuration

import kneat.evolution.genome.genes.attributes.configuration.*
import kneat.evolution.genome.genes.configuration.ConnectionConfiguration
import kneat.evolution.genome.genes.configuration.NodeConfiguration

/**
 * Used to pass necessary information to a [kneat.evolution.genome.Genome] to configure how it behaves.
 *
 * @property activationConfig The [ActivationConfiguration] for the genome
 * @property aggregationConfiguration The [aggregationConfiguration] for the genome
 * @property biasConfiguration The [biasConfiguration] for the genome
 * @property responseConfiguration The [responseConfiguration] for the genome
 * @property weightConfiguration The [weightConfiguration] for the genome
 * @property compatibilityConfiguration The [compatibilityConfiguration] for the genome
 * @property connectionConfiguration The [connectionConfiguration] for the genome
 * @property nodeConfiguration The [nodeConfiguration] for the genome
 * @property structureConfiguration The [structureConfiguration] for the genome
 */
data class GenomeConfiguration(
    val activationConfig: ActivationConfiguration = ActivationConfiguration(),
    val aggregationConfiguration: AggregationConfiguration = AggregationConfiguration(),
    val biasConfiguration: BiasConfiguration,
    val responseConfiguration: ResponseConfiguration = ResponseConfiguration(),
    val weightConfiguration: WeightConfiguration,
    val compatibilityConfiguration: CompatibilityConfiguration,
    val connectionConfiguration: ConnectionConfiguration,
    val nodeConfiguration: NodeConfiguration,
    val structureConfiguration: StructureConfiguration = StructureConfiguration()
)