package com.david.lamar.kneat.util.reporting

import com.david.lamar.kneat.Pipeline
import com.david.lamar.kneat.genome.Genome

interface Reporter : Pipeline.PipelineStateCallback {
    fun info(info: String)
    fun warn(warning: String)
    fun error(error: String, throwable: Throwable? = null)
    //fun onSpeciesStagnant(species: Species) TODO: Re-add this?
}

fun List<Reporter>.report() : Reporter {
    val list = this

    return object : Reporter {
        override fun info(info: String) = list.forEach { it.info(info) }
        override fun warn(warning: String) = list.forEach { it.warn(warning) }
        override fun error(error: String, throwable: Throwable?) = list.forEach { it.error(error, throwable) }
        override fun onCreatingInitialPopulation() = list.forEach { it.onCreatingInitialPopulation() }
        override fun onSpeciating() = list.forEach { it.onSpeciating() }
        override fun onSpeciated() = list.forEach { it.onSpeciated()}
        override fun onEvaluating(genome: Genome) = list.forEach { it.onEvaluating(genome) }
        override fun onEvaluated() = list.forEach { it.onEvaluated() }
        override fun onGenerationStarted() = list.forEach { it.onGenerationStarted() }
        override fun onGenerationFinished() = list.forEach { it.onGenerationFinished()}
        override fun onCulling() = list.forEach { it.onCulling() }
        override fun onCulled() = list.forEach { it.onCulled() }
        override fun onReproducing() = list.forEach { it.onReproducing() }
        override fun onReproduced() = list.forEach { it.onReproduced()}
        override fun onExtinction() = list.forEach { it.onExtinction() }
        override fun onSolutionFound(fittest: Genome?) = list.forEach { it.onSolutionFound(fittest) }
        override fun onPaused(willResumeState: Pipeline.PipelineState) = list.forEach { it.onPaused(willResumeState) }
        override fun onSaving(wilLResumeState: Pipeline.PipelineState) = list.forEach { it.onSaving(wilLResumeState) }
        override fun onLoading() = list.forEach { it.onLoading() }
    }
}

