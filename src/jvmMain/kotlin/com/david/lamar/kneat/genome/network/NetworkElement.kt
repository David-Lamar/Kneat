package com.david.lamar.kneat.genome.network

import com.david.lamar.kneat.genome.genes.Gene

/**
 * An element present in a [Network]; Generally a [Connection] or a [Node] but is open for
 * implementation in cases not covered by those structures.
 */
abstract class NetworkElement<ID, GENE : Gene> {

    /**
     * The unique identifier for this element
     */
    abstract val id: ID

    /**
     * The [Gene] that will manage the attributes of this element
     */
    abstract val gene: GENE
}
