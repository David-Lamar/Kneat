package com.david.lamar.kneat

import com.david.lamar.kneat.configuration.Configuration
import com.david.lamar.kneat.evolution.reproduction.KneatReproductionScheme
import com.david.lamar.kneat.evolution.reproduction.ReproductionScheme
import com.david.lamar.kneat.evolution.species.KneatSpeciationScheme
import com.david.lamar.kneat.evolution.species.SpeciationScheme
import com.david.lamar.kneat.evolution.species.Species
import com.david.lamar.kneat.evolution.stagnation.KneatStagnationScheme
import com.david.lamar.kneat.evolution.stagnation.StagnationScheme
import com.david.lamar.kneat.genome.EvaluationFunction
import com.david.lamar.kneat.genome.Genome
import com.david.lamar.kneat.util.report
import com.david.lamar.kneat.util.reporting.Reporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object Pipeline {
    private val pipelineContext: CoroutineScope = CoroutineScope(
        context = SupervisorJob() +
                Dispatchers.IO
    )

    // ********* Algorithm Data
    private var generation = 1L
    private var species: List<Species>? = null
    private var bestGenome: Genome? = null
    private lateinit var population: List<Genome>

    // ********* Configurable items
    private lateinit var config: Configuration
    private lateinit var evaluationFunction: EvaluationFunction
    private var stateListeners = mutableListOf<PipelineStateCallback>()
    private var reporters = mutableListOf<Reporter>()
    private var stagnation: StagnationScheme = KneatStagnationScheme()
    private var reproduction: ReproductionScheme = KneatReproductionScheme()
    private var speciationScheme: SpeciationScheme = KneatSpeciationScheme()

    // ********* Internal state helpers
    private val _state = MutableStateFlow<PipelineState>(PipelineState.Genesis)
    private var shouldPause: AtomicBoolean = AtomicBoolean(false)
    private var shouldSave: AtomicBoolean = AtomicBoolean(false)
    private var shouldLoad: AtomicBoolean = AtomicBoolean(false)

    val state: StateFlow<PipelineState> = _state

    // Configuration functions

    fun withEvaluationFunction(evaluationFunction: EvaluationFunction) : Pipeline {
        this.evaluationFunction = evaluationFunction
        return this
    }

    fun withReporter(reporter: Reporter) : Pipeline {
        this.reporters.add(reporter)
        this.stateListeners.add(reporter)
        return this
    }

    fun withStateListener(stateListener: PipelineStateCallback) : Pipeline {
        stateListeners.add(stateListener)
        return this
    }

    fun withConfiguration(config: Configuration) : Pipeline {
        this.config = config
        return this
    }

    fun withReproductionScheme(reproductionScheme: ReproductionScheme) : Pipeline {
        this.reproduction = reproductionScheme
        return this
    }

    fun withStagnationScheme(stagnationScheme: StagnationScheme) : Pipeline {
        this.stagnation = stagnationScheme
        return this
    }

    fun withSpeciationScheme(speciationScheme: SpeciationScheme) : Pipeline {
        this.speciationScheme = speciationScheme
        return this
    }

    // ******* Pipeline Management Functions

    suspend fun start() : Pipeline {
        if (!this::evaluationFunction.isInitialized) {
            error("Evaluation function needs to be specified")
        }

        if (!this::config.isInitialized) {
            error("Configuration needs to be specified")
        }

        _state.collect {
            if (it !is PipelineState.ExecutionState && determinePauseState(it)) {
                return@collect
            }

            // The following are our triggers to continue the algorithm; if a state is in "___ing" we're not going to do anything
            when (state.value) {
                is PipelineState.Genesis -> {
                    createInitialPopulation()
                }
                is PipelineState.InitialPopulationCreated -> {
                    speciatePopulation()
                }
                is PipelineState.Speciated -> {
                    startNextGeneration()
                }
                is PipelineState.Evaluated -> {
                    collectGenerationMetaData()
                }
                is PipelineState.GenerationStarted -> {
                    evaluateCurrentGeneration(evaluationFunction)
                }
                is PipelineState.GenerationFinished -> {
                    reproduce()
                }
                is PipelineState.ReproducedPopulation -> {
                    cull()
                }
                is PipelineState.Culled -> {
                    if (population.isEmpty()) {
                        notify(PipelineState.Extinction)
                    } else {
                        speciatePopulation()
                    }
                }
            }
        }

        return this
    }

    // Stops the current flow of the pipeline; however, if there is something currently running, it will wait for that to complete
    fun pause() : Pipeline {
        shouldPause.set(true)
        return this
    }

    // Resumes the current flow of the pipeline. Used to restart after pause is called
    suspend fun resume() = withContext(pipelineContext.coroutineContext) {
        val currentState = _state.value

        if (currentState !is PipelineState.ExecutionState.Paused) {
            error("Pipeline is not currently paused")
        } else {
            shouldPause.set(false)
            notify(currentState.resumeState)
        }

        return@withContext this@Pipeline
    }

    // Automatically pauses the pipeline if running
    suspend fun loadFrom(file: File) = withContext(pipelineContext.coroutineContext) {
        shouldLoad.set(true)
        //TODO: Need to store the file reference to use during the load function
        return@withContext this@Pipeline
    }

    // Automatically pauses the pipeline if running
    suspend fun saveTo(file: File) = withContext(pipelineContext.coroutineContext) {
        shouldSave.set(true)
        //TODO: Need to store the file reference to use during the save function
        return@withContext this@Pipeline
    }

    // ****** Private pipeline management functions

    private suspend fun determinePauseState(wantedState: PipelineState) : Boolean {
        return when {
            shouldPause.get() -> {
                shouldPause.set(false)
                notify(PipelineState.ExecutionState.Paused(resumeState = wantedState))
                true
            }
            shouldSave.get() -> {
                shouldSave.set(false)
                save(resumeState = wantedState)
                true
            }
            shouldLoad.get() -> {
                shouldLoad.set(false)
                load()
                true
            }
            else -> {
                false
            }
        }
    }

    private suspend fun save(resumeState: PipelineState) {
        notify(PipelineState.ExecutionState.Saving(resumeState))

        // TODO : Save the resume state as well so when we load, we can go to the next state

        notify(resumeState)
    }

    private suspend fun load() {
        notify(PipelineState.ExecutionState.Loading)

        //TODO: Get the state from the loaded pipeline
        val loadedResumeState = PipelineState.Genesis


        shouldPause.set(false)
        shouldSave.set(false)
        shouldLoad.set(false)
        notify(loadedResumeState)
    }

    private suspend fun createInitialPopulation() {
        notify(PipelineState.ExecutionState.CreatingInitialPopulation)

        population = reproduction.create(
            config.genomeConfiguration,
            config.populationSize,
            reporters = reporters
        )

        notify(PipelineState.InitialPopulationCreated)
    }

    private suspend fun speciatePopulation() {
        notify(PipelineState.ExecutionState.Speciating)

        species = speciationScheme.speciate(
            config.genomeConfiguration.compatibilityConfiguration.threshold,
            population = population,
            fitnessAggregationFunction = config.stagnationConfiguration.fitnessAggregationFunction,
            reporters = reporters,
            existingSpecies = species,
            generation = generation
        )

        notify(PipelineState.Speciated)
    }

    private suspend fun startNextGeneration() {
        generation++
        notify(PipelineState.GenerationStarted)
    }

    private suspend fun cull() {
        notify(PipelineState.ExecutionState.Culling)

        species = stagnation.killOffStagnant(
            species = species ?: emptyList(),
            config = config.stagnationConfiguration,
            generation = generation,
            reporters = reporters
        )

        notify(PipelineState.Culled)
    }

    private suspend fun reproduce() {
        notify(PipelineState.ExecutionState.ReproducingPopulation)

        population = reproduction.reproduce(
            config = config.reproductionConfiguration,
            generation = generation,
            populationSize = config.populationSize,
            species = species ?: emptyList(),
            reporters = reporters
        )

        notify(PipelineState.ReproducedPopulation)
    }

    private suspend fun evaluateCurrentGeneration(evaluationFunction: EvaluationFunction) {
        population.forEach {
            notify(PipelineState.ExecutionState.Evaluating(it))
            it.evaluate(evaluationFunction, generation)
            reporters.report().info("Evaluated genome ${it.key}; fitness was ${it.getFitness(generation)}")
        }

        notify(PipelineState.Evaluated)
    }

    private suspend fun collectGenerationMetaData() {
        notify(PipelineState.ExecutionState.CollectingMetaData)

        population.maxByOrNull { it.getFitness(generation) }?.let {
            val greatest = bestGenome
            if (greatest == null || it.getFitness(generation) > greatest.getFitness(generation)) {
                bestGenome = it
                //TODO: Report best of the best updated?
            }
        }

        var solutionFound = false
        config.terminationCriterion?.let {
            val totalFitness = it.fitnessAggregationFunction.aggregate(population.map { genome -> genome.getFitness(generation) })
            if (totalFitness >= it.fitnessThreshold) {
                solutionFound = true
                return@let
            }
        }

        if (solutionFound) {
            notify(PipelineState.SolutionFound(bestGenome!!))
        } else {
            notify(PipelineState.GenerationFinished)
        }
    }

    private suspend fun notify(state: PipelineState) {
        _state.emit(state)
        stateListeners.forEach { it.notify(state) }
    }

    sealed class PipelineState {
        object Genesis : PipelineState()

        sealed class ExecutionState : PipelineState() {
            object CreatingInitialPopulation : ExecutionState()
            object Speciating : ExecutionState()
            class Evaluating(val genome: Genome) : ExecutionState()
            object CollectingMetaData : ExecutionState()
            object Culling : ExecutionState()
            object ReproducingPopulation : ExecutionState()
            class Paused(val resumeState: PipelineState) : ExecutionState()
            object Loading : ExecutionState()
            class Saving(val resumeState: PipelineState) : ExecutionState()
        }

        object InitialPopulationCreated : PipelineState()
        object Speciated : PipelineState()
        object Evaluated : PipelineState()
        object GenerationFinished : PipelineState()
        object GenerationStarted : PipelineState()
        object Culled : PipelineState()
        object ReproducedPopulation : PipelineState()

        object Extinction : PipelineState()
        data class SolutionFound(val solution: Genome) : PipelineState()
    }

    interface PipelineStateCallback {
        fun onCreatingInitialPopulation()
        fun onSpeciating()
        fun onSpeciated()
        fun onEvaluating(genome: Genome)
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

        fun notify(state: PipelineState) {
            when (state) {
                is PipelineState.ExecutionState.CreatingInitialPopulation -> onCreatingInitialPopulation()
                is PipelineState.ExecutionState.Speciating -> onSpeciating()
                is PipelineState.Speciated -> onSpeciated()
                is PipelineState.ExecutionState.Evaluating -> onEvaluating(state.genome)
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
}