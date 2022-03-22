package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import kotlin.random.Random

inline fun <reified T> Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>.getAs(key: String) : T {
    return this.getOrElse(key) { error("Invalid gene configuration. ") } as T
}

abstract class Gene {

    /**
     * The attributes managed by this gene; indexed by a string name. Genes of the same type *MUST* have
     * the same list of managed attributes; otherwise, distance checking and crossover will fail.
     */
    abstract val managedAttributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>

    /**
     * Calculates the "distance" between two genes; see the implementor classes for implementation
     * specific details about how distance is calculated.
     */
    abstract fun distance(other: Gene) : Float

    /**
     * Copies a Gene; this is mostly a helper function to "update" a gene immutably
     */
    abstract fun copy(attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>) : Gene

    /**
     * Used during reproduction to combine two, same-type genes. A new gene is created with genes randomly
     * selected from this Gene and [other].
     *
     * If two genes aren't the same type (i.e. A [ConnectionGene] and a [NodeGene]) and an attempt is made to crossover,
     * an [IllegalStateException] will be thrown.
     */
    fun crossover(other: Gene) : Gene {
        val attributes = mutableMapOf<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>()
        managedAttributes.forEach { (s, _) ->
            val rand = Random.nextBoolean()
            if (rand) {
                attributes[s] = managedAttributes.getAs(s)
            } else {
                attributes[s] = other.managedAttributes.getAs(s)
            }
        }

        return copy(attributes)
    }
}
