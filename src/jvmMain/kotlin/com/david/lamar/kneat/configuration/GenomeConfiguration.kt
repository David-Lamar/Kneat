package com.david.lamar.kneat.configuration

import com.david.lamar.kneat.configuration.GenomeConfiguration.ActivationConfiguration
import com.david.lamar.kneat.configuration.GenomeConfiguration.ConnectionConfiguration.ConnectionType
import com.david.lamar.kneat.genome.network.Activation
import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.util.FloatRange
import com.david.lamar.kneat.util.MultiplierType

/**
 * Used to pass necessary information to a [com.david.lamar.kneat.genome.Genome] to configure how it behaves.
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
    val activationConfig: ActivationConfiguration,
    val aggregationConfiguration: AggregationConfiguration,
    val biasConfiguration: BiasConfiguration,
    val responseConfiguration: ResponseConfiguration,
    val weightConfiguration: WeightConfiguration,
    val compatibilityConfiguration: CompatibilityConfiguration,
    val connectionConfiguration: ConnectionConfiguration,
    val nodeConfiguration: NodeConfiguration,
    val structureConfiguration: StructureConfiguration = StructureConfiguration()
) {

    /**
     * Used to configure different managed attributes for a [com.david.lamar.kneat.genome.genes.Gene]
     *
     * @property default The default value the attribute will be assigned. If null, the initial value will be
     * determined by the implementing class.
     * @property replaceRate The probability for when a value will be entirely replaced with the initial value
     * calculated by the gene, or [default] during reproduction
     * @property mutationRate The probability that an attribute's value will be altered during reproduction
     */
    abstract class AttributeConfiguration<T> {
        open val default: T? = null
        open val replaceRate: Float = 0f
        abstract val mutationRate: Float
    }

    /**
     * Used to configure the [Activation] function used by a [com.david.lamar.kneat.genome.network.Node] via its
     * [com.david.lamar.kneat.genome.genes.NodeGene].
     *
     * @property default see [AttributeConfiguration]. If null, a random value out of the [available] pool will be
     * selected
     * @property mutationRate see [AttributeConfiguration]
     * @property available The list of available [Activation] functions that we may pull from for this node to use during
     * mutation. Defaulted to all provided activation functions; see [Activation]
     */
    data class ActivationConfiguration(
        override val default: Activation? = null,
        override val mutationRate: Float = 0f,
        val available: List<Activation> = listOf(
            Activation.AbsoluteValue, Activation.Clamped, Activation.Cubic, Activation.Exponential,
            Activation.ExponentialLinearUnit, Activation.Gaussian, Activation.Hat,
            Activation.HyperbolicTangent, Activation.Identity, Activation.Inverse,
            Activation.LeakyRectifiedLinearUnit, Activation.Logarithmic, Activation.RectifiedLinearUnit,
            Activation.ScaledExponentialLinearUNit, Activation.Sigmoid, Activation.Sin, Activation.Softplus,
            Activation.Square
        ),
    ) : AttributeConfiguration<Activation>()

    /**
     * Used to configure the [Aggregation] function used by a [com.david.lamar.kneat.genome.network.Node] via its
     * [com.david.lamar.kneat.genome.genes.NodeGene].
     *
     * @property default see [AttributeConfiguration]. If null, a random value out of the [available] pool will be
     * selected
     * @property mutationRate see [AttributeConfiguration]
     * @property available The list of available [Aggregation] functions that we may pull from for this node to use
     * during mutation. Defaulted to all provided aggregation functions; see [Aggregation]
     */
    data class AggregationConfiguration(
        override val default: Aggregation? = null,
        override val mutationRate: Float = 0f,
        val available: List<Aggregation> = listOf(
            Aggregation.Max, Aggregation.MaxAbsoluteValue, Aggregation.Mean, Aggregation.Median,
            Aggregation.Min, Aggregation.Product, Aggregation.Sum, Aggregation.Variance, Aggregation.StandardDeviation
        ),
    ) : AttributeConfiguration<Aggregation>()

    /**
     * Used to configure a [Boolean] value controlled by a [com.david.lamar.kneat.genome.genes.Gene]. Primary use case
     * is for the "enabled" state of a [com.david.lamar.kneat.genome.network.Connection]
     *
     * @property default see [AttributeConfiguration]; If null, a random [Boolean] is chosen
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     */
    abstract class BooleanConfiguration : AttributeConfiguration<Boolean>()

    /**
     * Used to configure a [Float] value controlled by a [com.david.lamar.kneat.genome.genes.Gene]. Primary use cases
     * are for "Bias" and "Response" for a [com.david.lamar.kneat.genome.network.Node] as well as the "Weight"
     * for a [com.david.lamar.kneat.genome.network.Connection]
     *
     * @property default see [AttributeConfiguration]; if null, will be a random value calculated via [initialMean],
     * [initialStandardDeviation], and [multiplierType]. See [multiplierType] for more information about this
     * calculation.
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     * @property mutatePower During a mutation, this value is multiplied by the zero-centered normal distribution and
     * added to the existing value of an [com.david.lamar.kneat.genome.genes.attributes.Attribute]. This is essentially
     * how drastic of a change will happen to the attribute value during a mutation.
     * @property initialMean If a [default] isn't specified, will be used to calculate the initial value; see
     * [multiplierType] for more details about this calculation.
     * @property initialStandardDeviation If a [default] isn't specified, will be used to calculate the initial value; see
     * [multiplierType] for more details about this calculation.
     * @property multiplierType If a [default] isn't specified, when this value is [MultiplierType.GAUSSIAN] or
     * [MultiplierType.NORMAL] the attribute's initial value will be a random value established via a
     * normal (gaussian) distribution zero-centered around the [initialStandardDeviation] and [initialMean]. When this
     * value is [MultiplierType.UNIFORM], the attribute's initial value will be calculated from a uniformly
     * distributed random value between [minValue] and [maxValue]. In all cases, the resolved value will be constrained
     * between [minValue] and [maxValue] if defined.
     * @property minValue The minimum allowed value of this attribute. When null, a minimum value of [Float.MIN_VALUE]
     * will be used.
     * @property maxValue The maximum allowed value of this attribute. When null, a maximum value of [Float.MAX_VALUE]
     * will be used.
     */
    abstract class FloatConfiguration : AttributeConfiguration<Float>() {
        abstract override val default: Float?
        abstract override val mutationRate: Float
        abstract override val replaceRate: Float
        abstract val mutatePower: Float
        abstract val initialMean: Float
        abstract val initialStandardDeviation: Float
        abstract val multiplierType: MultiplierType
        abstract val maxValue: Float?
        abstract val minValue: Float?
    }

    /**
     * Used to configure the Bias value used in a [com.david.lamar.kneat.genome.network.Node]. For more information
     * about how Bias affects a node, see [com.david.lamar.kneat.genome.network.Node].
     *
     * @property default see [AttributeConfiguration]
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     * @property mutatePower see [FloatConfiguration]
     * @property initialMean see [FloatConfiguration]
     * @property initialStandardDeviation see [FloatConfiguration]
     * @property multiplierType see [FloatConfiguration]
     * @property maxValue see [FloatConfiguration]
     * @property minValue see [FloatConfiguration]
     */
    data class BiasConfiguration(
        override val default: Float? = null,
        override val replaceRate: Float,
        override val mutationRate: Float,
        override val initialMean: Float,
        override val initialStandardDeviation: Float,
        override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
        override val maxValue: Float? = null,
        override val minValue: Float? = null,
        override val mutatePower: Float
    ) : FloatConfiguration()

    /**
     * Used to configure the Response value used in a [com.david.lamar.kneat.genome.network.Node]. For more information
     * about how Response affects a node, see [com.david.lamar.kneat.genome.network.Node].
     *
     * @property default see [AttributeConfiguration]
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     * @property mutatePower see [FloatConfiguration]
     * @property initialMean see [FloatConfiguration]
     * @property initialStandardDeviation see [FloatConfiguration]
     * @property multiplierType see [FloatConfiguration]
     * @property maxValue see [FloatConfiguration]
     * @property minValue see [FloatConfiguration]
     */
    data class ResponseConfiguration(
        override val default: Float? = null,
        override val mutationRate: Float,
        override val replaceRate: Float,
        override val initialMean: Float,
        override val initialStandardDeviation: Float,
        override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
        override val maxValue: Float? = null,
        override val minValue: Float? = null,
        override val mutatePower: Float
    ) : FloatConfiguration()

    /**
     * Used to configure the Weight value used in a [com.david.lamar.kneat.genome.network.Connection]. For more
     * information about how Weight affects a connection, see [com.david.lamar.kneat.genome.network.Connection].
     *
     * @property default see [AttributeConfiguration]
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     * @property mutatePower see [FloatConfiguration]
     * @property initialMean see [FloatConfiguration]
     * @property initialStandardDeviation see [FloatConfiguration]
     * @property multiplierType see [FloatConfiguration]
     * @property maxValue see [FloatConfiguration]
     * @property minValue see [FloatConfiguration]
     */
    data class WeightConfiguration(
        override val default: Float? = null,
        override val mutationRate: Float,
        override val replaceRate: Float,
        override val initialMean: Float,
        override val initialStandardDeviation: Float,
        override val multiplierType: MultiplierType = MultiplierType.GAUSSIAN,
        override val maxValue: Float? = null,
        override val minValue: Float? = null,
        override val mutatePower: Float
    ) : FloatConfiguration()

    /**
     * # Used to determine how genetically similar two genomes are.
     *
     * Distance between two genomes is calculated by adding two values:
     * 1. Taking the [com.david.lamar.kneat.genome.genes.Gene.distance] for each gene in each genome multiplied by the
     * [weightCoefficient] and adding them together; referred to as the gene distance
     * 2. The number of disjoint nodes between the two genomes multiplied by the [disjointCoefficient]; referred to as
     * the disjoint distance
     *
     * ## Calculating Gene Distance
     *
     * Example: You have two genomes;
     * Genome A has gene[1] with a managed FloatAttribute of 1.0, gene[2] with a managed FloatAttribute of 0.8
     * Genome B has gene[1] with a managed FloatAttribute of 1.2, gene[2] with a managed FloatAttribute of 1.0
     *
     * We will check each difference for each gene. So, Genome A, gene[1] has a value of 1.0 and Genome B, gene[1] has
     * a value of 1.2. The absolute difference between these is 0.2. If we look at both gene[2]'s, we see they have the
     * same difference of 0.2.
     *
     * Each of these values would get multiplied by the [weightCoefficient] and then added together, so the total gene
     * distance between these two genomes would be:
     *
     * geneDistance = (0.2 * [weightCoefficient]) + (0.2 * [weightCoefficient])
     *
     * For more information about how this impacts the the genome population, see [com.david.lamar.kneat.evolution.species.SpeciationScheme].
     *
     *
     * ## Calculating Disjoint Distance
     * Example: You have two genomes;
     * Genome A has 1 input node (Node[-1]), 2 hidden nodes (Node[2] and Node[3]), and 1 output node(Node[1]).
     * Genome B has 1 input node (Node[-1]), 1 hidden node (Node[4], NOTE: Has genetically different ancestry from
     * Node[2] and Node[3] above]]), and 1 output node (Node[1]).
     *
     * We would calculate the number of disjoint nodes by seeing which nodes in the network are different; in this case
     * Node[2] and Node[3] are not present in Genome B; similarly, Node[4] is not present in Genome A. All other nodes
     * share similar ancestry.
     *
     * In this example, the number of disjoint nodes is 3; so [disjointCoefficient] would be multiplied by 3, so
     * the total disjoint node distance would be:
     *
     * disjointDistance = 3 * [disjointCoefficient]
     *
     * @property threshold The value at which the "distance" between two genomes is considered similar enough to be in
     * the same species.
     * @property disjointCoefficient This value is a multiplier applied to the number of "disjoint" nodes across two
     * genomes. A node is considered "disjoint" in a genome if it's not present in the other genome.
     * @property weightCoefficient This value is a multiplier applied to each distance of each gene across the two
     * genomes.
     */
    data class CompatibilityConfiguration(
        val threshold: Float,
        val disjointCoefficient: Float,
        val weightCoefficient: Float
    )

    /**
     * Used to configure how connections behave across a genome.
     *
     * @property default see [AttributeConfiguration] //TODO: This value is currently unused; it should be
     * @property mutationRate see [AttributeConfiguration]
     * @property replaceRate see [AttributeConfiguration]
     * @property additionProbability The probability that a connection will get added to the network on any given mutation
     * @property deletionProbability The probability that a connection will get deleted from the network on any given mutation
     * @property allowRecurrence Whether or not nodes can link to themselves OR may cause loops in the network. Note: If
     * this is set to false, there is a moderate computational penalty associated with calculating a connection addition
     * as it has to avoid causing cycles in the network. If you have a lot of nodes, a high [additionProbability], or
     * a slow computer it may be worth while allowing recurrence even if it's not explicitly necessary for your
     * problem domain.
     * @property initialConnection The initial [ConnectionType] used when a connection is created
     */
    data class ConnectionConfiguration(
        override val default: Boolean? = null,
        override val replaceRate: Float,
        override val mutationRate: Float,
        val additionProbability: Float,
        val deletionProbability: Float,
        val allowRecurrence: Boolean,
        val initialConnection: ConnectionType = ConnectionType.Unconnected
    ) : BooleanConfiguration() {

        /**
         * Different methods of initial connectivity between nodes when a genome is created
         */
        sealed class ConnectionType {

            /**
             * All nodes by default have no connections to each other.
             */
            object Unconnected : ConnectionType()

            /**
             * A method of creating connections from a randomly selected input node. Note: Input nodes will never
             * connect to another input node
             */
            sealed class SingleSelection : ConnectionType() {
                /**
                 * A connection will be created from a randomly selected input node to all hidden nodes and no output
                 * nodes.
                 */
                object HiddenOnly : SingleSelection()

                /**
                 * A connection will be created from a randomly selected input node to all output nodes and no
                 * hidden nodes, if present.
                 */
                object OutputOnly : SingleSelection()

                /**
                 * A connection will be created from a randomly selected input node to all, non-input nodes in the
                 * network.
                 */
                object All : SingleSelection()
            }

            /**
             * A method of creating connections from multiple selected inputs. Whether or not an input is
             * considered is determined randomly by the provided [probability]. Note: Input nodes will never
             * connect to another input node
             */
            sealed class PartialSelection : ConnectionType() {
                @FloatRange(0.0, 1.0)
                abstract val probability: Float

                /**
                 * A connection will be created from each randomly selected input node to all hidden nodes and no output
                 * nodes.
                 */
                class HiddenOnly(override val probability: Float) : PartialSelection()

                /**
                 * A connection will be created from each randomly selected input node to all output nodes and no
                 * hidden nodes, if present.
                 */
                class OutputOnly(override val probability: Float) : PartialSelection()

                /**
                 * A connection will be created from each randomly selected input node to all hidden nodes and all
                 * output nodes
                 */
                class All(override val probability: Float) : PartialSelection()
            }

            /**
             * A method of creating connections from all inputs. Note: Input nodes will never connect to another input node
             */
            sealed class FullSelection : ConnectionType() {
                /**
                 * A connection will be created from all input nodes to all hidden nodes and no output nodes
                 */
                object HiddenOnly : FullSelection()

                /**
                 * A connection will be created from all input nodes to all output nodes and no hidden nodes if present
                 */
                object OutputOnly : FullSelection()

                /**
                 * A connection will be created from all input nodes to all output and hidden nodes
                 */
                object All : FullSelection()
            }
        }
    }

    /**
     * Used to configure how nodes behave across a genome.
     *
     * @property additionProbability The probability that a hidden node will get added to the network during any given mutation
     * @property deletionProbability The probability that a hidden node will get deleted from the network during any given mutation
     * @property initialHidden The initial number of hidden nodes in the network
     * @property inputs The number of inputs to the network
     * @property outputs The number of outputs to the network
     */
    data class NodeConfiguration(
        val additionProbability: Float,
        val deletionProbability: Float,
        val initialHidden: Int = 0,
        val inputs: Int,
        val outputs: Int
    )

    /**
     * Used to configure aspects about the structure of the genome
     *
     * @property allowedStructuralMutations How many structural mutations (changes in nodes, connections, etc.) are
     * allowed during any given mutation
     * @property ensureStructuralMutation If true and there are no connections in the network, additional nodes will
     * not be created until connections are present. During a mutation, if a node is scheduled to be added, a connection
     * will be instead.
     *
     * //TODO: should probably put recurrence here instead of connection configuration as it is more structure oriented
     */
    data class StructureConfiguration(
        val allowedStructuralMutations: Int = Int.MAX_VALUE,
        val ensureStructuralMutation: Boolean = false
    )
}