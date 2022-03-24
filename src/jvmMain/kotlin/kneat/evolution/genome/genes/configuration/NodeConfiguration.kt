package kneat.evolution.genome.genes.configuration

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