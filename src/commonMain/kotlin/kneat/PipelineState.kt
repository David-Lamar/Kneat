package kneat

import kneat.evolution.genome.Genome

/**
 * The different states that the Pipeline may have. Can be observed via the [state] object or by adding
 * a state listener via [withStateListener].
 */
sealed class PipelineState {

    /**
     * The initial state of a pipeline; this just tells us that the pipeline hasn't begun execution yet
     */
    object Genesis : PipelineState()

    /**
     * States that fall in the category of an "execution", or, a longer running task that should not be
     * interrupted
     */
    sealed class ExecutionState : PipelineState() {

        /**
         * Signals that we're creating the very first population to be used in the simulation
         */
        object CreatingInitialPopulation : ExecutionState()

        /**
         * Signals that we're speciating the current population
         */
        object Speciating : ExecutionState()

        /**
         * Signals that we're evaluating a genome in the population
         *
         * @property genome The [Genome] currently being evaluated
         */
        class Evaluating(val genome: Genome) : ExecutionState()

        /**
         * Signals that we're collecting meta data about the current generation
         */
        object CollectingMetaData : ExecutionState()

        /**
         * Signals that we're culling the current population
         */
        object Culling : ExecutionState()

        /**
         * Signals that we're reproducing individuals for the next generation
         */
        object ReproducingPopulation : ExecutionState()

        /**
         * Signals that the simulation has been paused
         *
         * @property resumeState The state that the pipeline will go to if the user resumes
         */
        class Paused(val resumeState: PipelineState) : ExecutionState()

        /**
         * Signals that the simulation is being loaded from a file
         */
        object Loading : ExecutionState()

        /**
         * Signals that the simulation is being saved to a file
         *
         * @property resumeState The state that the pipeline will go to if the user resumes
         */
        class Saving(val resumeState: PipelineState) : ExecutionState() //TODO: Resume state may not be needed in this state
    }

    /**
     * Signals that we're finished creating the initial population
     */
    object InitialPopulationCreated : PipelineState()

    /**
     * Signals that we've finished speciating the population
     */
    object Speciated : PipelineState()

    /**
     * Signals that we've evaluated all genomes in the current generation
     */
    object Evaluated : PipelineState()

    /**
     * Signals that we've finished everything related to the current generation
     */
    object GenerationFinished : PipelineState()

    /**
     * Signals that we've started a new generation
     */
    object GenerationStarted : PipelineState()

    /**
     * Signals that we've finished culling the current population
     */
    object Culled : PipelineState()

    /**
     * Signals that we've finished reproducing individuals for the next generation
     */
    object ReproducedPopulation : PipelineState()

    /**
     * Signals that we've finished saving the simulation to a file
     */
    class Saved(val resumeState: PipelineState) : PipelineState()

    /**
     * Signals that all individuals have died out
     */
    object Extinction : PipelineState()

    /**
     * Signals that a solution to the problem domain has been found according to the threshold
     * configured in [Configuration].
     *
     * @property solution The [Genome] with the highest fitness
     */
    data class SolutionFound(val solution: Genome) : PipelineState()
}