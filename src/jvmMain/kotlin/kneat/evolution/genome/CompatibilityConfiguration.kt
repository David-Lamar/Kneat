package kneat.evolution.genome

import kneat.evolution.genome.genes.Gene
import kneat.evolution.species.SpeciationScheme

/**
 * # Used to determine how genetically similar two genomes are.
 *
 * Distance between two genomes is calculated by adding two values:
 * 1. Taking the [Gene.distance] for each gene in each genome multiplied by the
 * [weightCoefficient] and adding them together; referred to as the gene distance
 * 2. The number of disjoint nodes between the two genomes multiplied by the [disjointCoefficient]; referred to as
 * the disjoint distance
 *
 * ## Calculating Gene Distance
 *
 * Example: You have two genomes;
 * Genome A has gene[1] with a managed FloatAttribute of 1.0, gene[2] with a managed FloatAttribute of 0.8
 * Genome B has gene[1] with a managed FloatAttribute of 1.2, gene[2] with a managed FloatAttribute of 1.0
 *
 * We will check each difference for each gene. So, Genome A, gene[1] has a value of 1.0 and Genome B, gene[1] has
 * a value of 1.2. The absolute difference between these is 0.2. If we look at both gene[2]'s, we see they have the
 * same difference of 0.2.
 *
 * Each of these values would get multiplied by the [weightCoefficient] and then added together, so the total gene
 * distance between these two genomes would be:
 *
 * geneDistance = (0.2 * [weightCoefficient]) + (0.2 * [weightCoefficient])
 *
 * For more information about how this impacts the the genome population, see [SpeciationScheme].
 *
 *
 * ## Calculating Disjoint Distance
 * Example: You have two genomes;
 * Genome A has 1 input node (Node[-1]), 2 hidden nodes (Node[2] and Node[3]), and 1 output node(Node[1]).
 * Genome B has 1 input node (Node[-1]), 1 hidden node (Node[4], NOTE: Has genetically different ancestry from
 * Node[2] and Node[3] above]]), and 1 output node (Node[1]).
 *
 * We would calculate the number of disjoint nodes by seeing which nodes in the network are different; in this case
 * Node[2] and Node[3] are not present in Genome B; similarly, Node[4] is not present in Genome A. All other nodes
 * share similar ancestry.
 *
 * In this example, the number of disjoint nodes is 3; so [disjointCoefficient] would be multiplied by 3, so
 * the total disjoint node distance would be:
 *
 * disjointDistance = 3 * [disjointCoefficient]
 *
 * @property threshold The value at which the "distance" between two genomes is considered similar enough to be in
 * the same species.
 * @property disjointCoefficient This value is a multiplier applied to the number of "disjoint" nodes across two
 * genomes. A node is considered "disjoint" in a genome if it's not present in the other genome.
 * @property weightCoefficient This value is a multiplier applied to each distance of each gene across the two
 * genomes.
 */
data class CompatibilityConfiguration(
    val threshold: Float,
    val disjointCoefficient: Float,
    val weightCoefficient: Float
)