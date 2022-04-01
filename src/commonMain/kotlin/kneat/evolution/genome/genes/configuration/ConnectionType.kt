package kneat.evolution.genome.genes.configuration

import kneat.util.FloatRange

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