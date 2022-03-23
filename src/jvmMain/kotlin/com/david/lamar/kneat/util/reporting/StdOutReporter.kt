package com.david.lamar.kneat.util.reporting

import com.david.lamar.kneat.Pipeline
import com.david.lamar.kneat.genome.Genome

/**
 * An implementation of a [Reporter] that prints the updates into the JVM console
 */
class StdOutReporter : Reporter {
    override fun info(info: String) {
        println("[INFO]: $info")
    }

    override fun warn(warning: String) {
        println("[WARNING]: $warning")
    }

    override fun error(error: String, throwable: Throwable?) {
        println("[ERROR]: $error : ${throwable?.message}")
    }

    override fun onCreatingInitialPopulation() {
        println("Creating initial population")
    }

    override fun onSpeciating() {
        println("Speciating")
    }

    override fun onSpeciated() {
        println("Speciation complete")
    }

    override fun onEvaluating(genome: Genome) {
        println("Evaluating genome: ${genome.key}...")
    }

    override fun onEvaluated() {
        println("Evaluation complete")
    }

    override fun onGenerationStarted() {
        println("Generation started")
    }

    override fun onGenerationFinished() {
        println("Generation ended")
    }

    override fun onCulling() {
        println("Culling...")
    }

    override fun onCulled() {
        println("Culling complete")
    }

    override fun onReproducing() {
        println("Reproducing...")
    }

    override fun onReproduced() {
        println("Reproduction complete")
    }

    override fun onExtinction() {
        println("Extinction")
    }

    override fun onSolutionFound(fittest: Genome?) {
        println("Solution found with Genome: ${fittest?.key}")
    }

    override fun onPaused(willResumeState: Pipeline.PipelineState) {
        println("Evolution paused")
    }

    override fun onSaving(wilLResumeState: Pipeline.PipelineState) {
        println("Saving pipeline state...")
    }

    override fun onLoading() {
        println("Loading pipeline state...")
    }
}