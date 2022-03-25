package kneat.evolution.genome.genes.attributes.configuration

/**
 * Used to configure different managed attributes for a [kneat.evolution.genome.genes.Gene]
 *
 * @property default The default value the attribute will be assigned. If null, the initial value will be
 * determined by the implementing class.
 * @property replaceRate The probability for when a value will be entirely replaced with the initial value
 * calculated by the gene, or [default] during reproduction
 * @property mutationRate The probability that an attribute's value will be altered during reproduction
 */
abstract class AttributeConfiguration<T> {
    open val default: T? = null
    open val replaceRate: Float = 0f
    abstract val mutationRate: Float
}