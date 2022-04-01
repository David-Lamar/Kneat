package kneat.evolution.network

import kneat.evolution.genome.genes.configuration.NodeConfiguration

/**
 * A computational Neural Network evolved via the NEAT algorithm. It takes in a specified
 * number of inputs (may be specified via [NodeConfiguration]) and produces a list of outputs (may be
 * specified via [NodeConfiguration]) that attempts to provide a solution for the problem domain.
 */
interface Network {

    /**
     * Given a specified [inputs], generates a list of values that attempt to provide a solution
     * for the problem domain.
     *
     * ### Note on Asynchronous Kneat Networks
     * The Kneat library is asynchronous by default. As such, it would make sense that the networks themselves
     * would be asynchronous. This means that as Nodes (neurons) are activating, there's no "ordering" or
     * "layering" about which nodes are getting activated, and when.
     *
     * *There's a few primary benefits to having a network behave in this way*;
     *
     * 1. Network execution is MUCH faster in all networks (and especially large ones). Since everything is
     * executing in parallel, many nodes can be firing simultaneously (or even all at once depending on
     * the computational power of your machine and the size of the network).
     * 2. The network behaves more similarly to a biological neural network.
     *
     * *Implications of asynchronous computational neural networks:*
     *
     * This system of asynchronous network evaluation does differ from "traditional" computational
     * neural networks in that nodes aren't evaluated synchronously. This means that with sufficiently
     * large networks, or, networks with cycles or recurrence, they need some time to "stabilize" to a
     * solution. This goes against our traditional intuition of "when a network has a solution, that's
     * it!" because in synchronous computational neural networks, we knew the solution we'd arrived at
     * was finished executing. With Asynchronous computational neural networks, we need to allow the network
     * time to stabilize to an "accurate" solution if the solution we're after is required to be static
     * (e.g. classification solutions).
     *
     * ### Configuring asynchronous behavior
     *
     * Currently the asynchronous behavior of the network is linked to the IO dispatcher provided by
     * Kotlin Coroutines. This limits the number of threads your network (and pipeline) can execute on to 64 OR the
     * number of cores on your machine -- whichever is larger.
     *
     * While this is a pretty substantial amount of threads for hobby usage, it limits the capabilities
     * of commercial solutions to the specifications of Kotlin Coroutines. As such, the current behavior
     * is ideally temporary; the library is still being developed and configurable asynchronicity is on my radar of
     * improvements that would greatly benefit the library for hobbyists and commercial solutions.
     */
    suspend fun activate(inputs: List<Float>, stabilizationDelay: Long) : List<Float>
}