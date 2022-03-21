package com.david.lamar.kneat.genome.genes.attributes

import com.david.lamar.kneat.configuration.GenomeConfiguration.BooleanConfiguration
import kotlin.random.Random

class BooleanAttribute(booleanConfiguration: BooleanConfiguration) : Attribute<Boolean, BooleanConfiguration>(booleanConfiguration) {

    override var value: Boolean = config.default ?: Random.nextBoolean()

    private var modifiableMutationRate: Float = config.mutationRate

    override fun mutate(): Boolean {
        modifiableMutationRate += config.replaceRate

        if (modifiableMutationRate > 0) {
            value = Random.nextBoolean()
            return value
        }

        return value
    }
}