package kneat.evolution.genome.genes.attributes.configuration

import kneat.evolution.genome.genes.Gene

/**
 * Used to configure a [Boolean] value controlled by a [Gene]. Primary use case
 * is for the "enabled" state of a [Connection]
 *
 * @property default see [AttributeConfiguration]; If null, a random [Boolean] is chosen
 * @property mutationRate see [AttributeConfiguration]
 * @property replaceRate see [AttributeConfiguration]
 */
abstract class BooleanConfiguration : AttributeConfiguration<Boolean>()