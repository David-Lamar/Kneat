package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.configuration.GenomeConfiguration

/**
 * Class that denotes an attribute that can be managed by a [com.david.lamar.kneat.genes.Gene]. All attributes must
 * share a common configuration defined in [GenomeConfiguration.AttributeConfiguration].
 */
abstract class Attribute<T, C : GenomeConfiguration.AttributeConfiguration<T>>(protected val config: C) {

    /**
     * The current value of the attribute. Setting is protected so that the attribute cannot be mutated outside
     * of the scope of itself.
     *
     * Abstract so that the default / initial value is enforced to be defined by the implementor
     */
    abstract var value: T
        protected set

    /**
     * Mutates the [value] of this attribute according to the parameters set by the [config].
     */
    abstract fun mutate() : T
}

