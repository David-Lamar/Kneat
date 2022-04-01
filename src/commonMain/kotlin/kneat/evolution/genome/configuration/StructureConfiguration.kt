package kneat.evolution.genome.configuration

/**
 * Used to configure aspects about the structure of the genome
 *
 * @property allowedStructuralMutations How many structural mutations (changes in nodes, connections, etc.) are
 * allowed during any given mutation
 * @property ensureStructuralMutation If true and there are no connections in the network, additional nodes will
 * not be created until connections are present. During a mutation, if a node is scheduled to be added, a connection
 * will be instead.
 *
 * //TODO: should probably put recurrence here instead of connection configuration as it is more structure oriented
 */
data class StructureConfiguration(
    val allowedStructuralMutations: Int = Int.MAX_VALUE,
    val ensureStructuralMutation: Boolean = false,
    val allowRecurrence: Boolean = true
)