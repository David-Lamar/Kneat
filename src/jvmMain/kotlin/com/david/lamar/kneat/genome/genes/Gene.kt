package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import kotlin.random.Random

inline fun <reified T> Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>.getAs(key: String) : T {
    return this.getOrElse(key) { error("Invalid gene configuration. ") } as T
}

abstract class Gene {

    abstract val managedAttributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>
    abstract fun distance(other: Gene) : Float
    abstract fun copy(attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>) : Gene

    //TODO: note that if crossed over with a non-same-type gene, it will have undefined behavior
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
