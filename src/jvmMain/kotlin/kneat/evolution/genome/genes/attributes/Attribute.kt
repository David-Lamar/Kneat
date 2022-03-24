package kneat.evolution.genome.genes.attributes

import kneat.evolution.genome.genes.attributes.configuration.AttributeConfiguration
import kneat.evolution.genome.genes.Gene

/**
 * Class that denotes an attribute that can be managed by a [Gene]. All attributes
 * must share a common configuration defined in [AttributeConfiguration].
 *
 * @param config The [AttributeConfiguration] used to configure this Attribute
 */
abstract class Attribute<T, C : AttributeConfiguration<T>>(protected val config: C) {

    /**
     * The current value of the attribute. Setting is protected so that the attribute cannot be mutated outside
     * of the scope of itself to promote immutability
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

