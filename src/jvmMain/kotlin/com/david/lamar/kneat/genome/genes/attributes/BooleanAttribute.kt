package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.configuration.GenomeConfiguration.BooleanConfiguration
import kotlin.random.Random

/**
 * A custom [Attribute] that manages a boolean value; primarily used within a
 * [com.david.lamar.kneat.genome.genes.ConnectionGene]. May be configured via
 * a [com.david.lamar.kneat.configuration.GenomeConfiguration.BooleanConfiguration].
 *
 * @param booleanConfiguration The [BooleanConfiguration] used to configure this attribute
 */
class BooleanAttribute(booleanConfiguration: BooleanConfiguration) : Attribute<Boolean, BooleanConfiguration>(booleanConfiguration) {

    override var value: Boolean = config.default ?: Random.nextBoolean()

    /**
     * Slightly different from [BooleanConfiguration.mutationRate] in that it will increase
     * over time based on the [BooleanConfiguration.replaceRate]
     */
    private var modifiableMutationRate: Float = config.mutationRate

    /**
     * Mutates the boolean by choosing a random boolean if the [modifiableMutationRate] has increased
     * above 0
     */
    override fun mutate(): Boolean {
        modifiableMutationRate += config.replaceRate

        if (modifiableMutationRate > 0) {
            value = Random.nextBoolean()
            return value
        }

        return value
    }
}