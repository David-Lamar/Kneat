package kneat

import kneat.evolution.genome.Genome

/**
     * Callback to receive different [PipelineState] updates; these functions have a 1-1 mapping
     * to the different [PipelineState]s.
     */
    interface PipelineStateCallback {
        fun onCreatingInitialPopulation()
        fun onSpeciating()
        fun onSpeciated()
        fun onEvaluating(speciesId: Long, genome: Genome)
        fun onEvaluated()
        fun onGenerationStarted()
        fun onGenerationFinished()
        fun onCulling()
        fun onCulled()
        fun onReproducing()
        fun onReproduced()
        fun onExtinction()
        fun onSolutionFound(fittest: Genome?)
        fun onPaused(willResumeState: PipelineState)
        fun onSaving(wilLResumeState: PipelineState)
        fun onLoading()

        /**
         * Helper function to notify a [PipelineStateCallback] when a specific [PipelineState] has been
         * emitted
         */
        fun notify(state: PipelineState) {
            when (state) {
                is PipelineState.ExecutionState.CreatingInitialPopulation -> onCreatingInitialPopulation()
                is PipelineState.ExecutionState.Speciating -> onSpeciating()
                is PipelineState.Speciated -> onSpeciated()
                is PipelineState.ExecutionState.Evaluating -> onEvaluating(state.speciesId, state.genome)
                is PipelineState.Evaluated -> onEvaluated()
                is PipelineState.GenerationStarted -> onGenerationStarted()
                is PipelineState.GenerationFinished -> onGenerationFinished()
                is PipelineState.ExecutionState.Culling -> onCulling()
                is PipelineState.Culled -> onCulled()
                is PipelineState.ExecutionState.ReproducingPopulation -> onReproducing()
                is PipelineState.ReproducedPopulation -> onReproduced()
                is PipelineState.Extinction -> onExtinction()
                is PipelineState.SolutionFound -> onSolutionFound(state.solution)
                is PipelineState.ExecutionState.Paused -> onPaused(state.resumeState)
                is PipelineState.ExecutionState.Saving -> onSaving(state.resumeState)
                is PipelineState.ExecutionState.Loading -> onLoading()
            }
        }
    }