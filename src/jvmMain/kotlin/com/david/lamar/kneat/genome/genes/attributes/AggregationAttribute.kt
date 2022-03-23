package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.configuration.GenomeConfiguration.AggregationConfiguration
import com.david.lamar.kneat.genome.network.Aggregation
import kotlin.random.Random

/**
 * A custom [Attribute] that manages the aggregation function of a [com.david.lamar.kneat.genome.network.Node]. May be
 * configured via [com.david.lamar.kneat.configuration.GenomeConfiguration.AggregationConfiguration].
 *
 * @param config The [AggregationConfiguration] used to configure this attribute
 */
class AggregationAttribute(config: AggregationConfiguration) : Attribute<Aggregation, AggregationConfiguration>(config) {

    /**
     * The possible [Aggregation] functions we can use in this attribute. May be configured via
     * [AggregationConfiguration.available].
     */
    private var availableOptions: List<Aggregation> = config.available

    override var value: Aggregation = config.default ?: availableOptions.random()

    /**
     * Mutates the aggregation function by choosing a random item from the [availableOptions] list if the
     * [AggregationConfiguration.mutationRate] is above 0 and @return the new [value]
     */
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