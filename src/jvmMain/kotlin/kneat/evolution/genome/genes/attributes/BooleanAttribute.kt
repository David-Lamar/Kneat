package kneat.evolution.genome.genes.attributes

import kneat.evolution.genome.genes.attributes.configuration.BooleanConfiguration
import kneat.evolution.genome.genes.ConnectionGene
import kotlin.random.Random

/**
 * A custom [Attribute] that manages a boolean value; primarily used within a
 * [ConnectionGene]. May be configured via a [BooleanConfiguration].
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