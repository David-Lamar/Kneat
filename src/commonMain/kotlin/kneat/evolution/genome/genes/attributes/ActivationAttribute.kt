package kneat.evolution.genome.genes.attributes

import kneat.evolution.genome.genes.attributes.configuration.ActivationConfiguration
import kneat.evolution.network.Activation
import kneat.evolution.network.Node
import kotlin.random.Random

/**
 * A custom [Attribute] that manages the activation function of a [Node]. May be configured via [ActivationConfiguration].
 *
 * @param config The [ActivationConfiguration] used to configure this attribute
 */
class ActivationAttribute(config: ActivationConfiguration) : Attribute<Activation, ActivationConfiguration>(config) {

    /**
     * The possible [Activation] functions we can use in this attribute. May be configured via
     * [ActivationConfiguration.available].
     */
    private var availableOptions: List<Activation> = config.available

    override var value: Activation = config.default ?: availableOptions.random()

    /**
     * Mutates the activation function by choosing a random item from the [availableOptions] list if the
     * [ActivationConfiguration.mutationRate] is above 0 and @return the new [value]
     */
    override fun mutate(): Activation {
        if (config.mutationRate > 0) {
            val rand = Random.nextFloat()
            if (rand > config.mutationRate) {
                value = availableOptions.random()
            }
        }

        return value
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ActivationAttribute) return false

        if (other.value == this.value) return true

        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}