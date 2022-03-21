package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.genome.network.Aggregation
import com.david.lamar.kneat.configuration.GenomeConfiguration.AggregationConfiguration
import kotlin.random.Random

class AggregationAttribute(config: AggregationConfiguration) : Attribute<Aggregation, AggregationConfiguration>(config) {

    private var availableOptions: List<Aggregation> = config.available
    override var value: Aggregation = config.default ?: availableOptions.random()

    override fun mutate(): Aggregation {
        if (config.mutationRate > 0) {
            val rand = Random.nextFloat()
            if (rand > config.mutationRate) {
                value = availableOptions.random()
            }
        }

        return value
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AggregationAttribute) return false

        if (other.value.javaClass == this.value.javaClass) return true

        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}