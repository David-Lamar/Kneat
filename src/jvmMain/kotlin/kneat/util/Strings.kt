package kneat.util

import kneat.evolution.genome.genes.configuration.ConnectionConfiguration
import kneat.evolution.genome.genes.configuration.NodeConfiguration

/**
 * String resource file so that all messages for the pipeline are stored in one place. This will allow
 * the library to be localized at some point.
 */

/**
 * Shown when a [ConnectionConfiguration] has a "Hidden Only" configuration specified but no initial hidden
 * nodes specified via [NodeConfiguration]
 */
const val WARNING_INITIAL_CONFIGURATION = "Configuration.genomeConfiguration.connectionConfiguration.initialConnection was set to %1\$s. However, there were no hidden nodes specified by Configuration.genomeConfiguration.nodeConfiguration.initialHidden. The behavior will fall back to %2\$s; if this is not desired, specify a value for initialHidden in the configuration. If this is desired, please consider using %2\$s as your initial connection configuration."

/**
 * Show when a specific species has died out during a culling event
 */
const val SPECIES_EXTINCTION_MESSAGE = "☠️ Species %1s has died out. R.I.P."