package kneat.evolution.genome

/**
 * A type alias for an evaluation function. Evaluation functions are used to evaluate the fitness
 * of a [Genome]
 */
typealias EvaluationFunction = suspend (genome: Genome) -> Float