package kneat

import kneat.evolution.configuration.Configuration
import kneat.evolution.configuration.TerminationCriterion
import kneat.evolution.genome.EvaluationFunction
import kneat.evolution.genome.Genome
import kneat.evolution.genome.KneatGenome
import kneat.evolution.network.Network
import kneat.evolution.reproduction.KneatReproductionScheme
import kneat.evolution.reproduction.ReproductionScheme
import kneat.evolution.species.KneatSpeciationScheme
import kneat.evolution.species.SpeciationScheme
import kneat.evolution.species.Species
import kneat.evolution.stagnation.KneatStagnationScheme
import kneat.evolution.stagnation.StagnationScheme
import kneat.util.caching.GenomeCache
import kneat.util.report
import kneat.util.reporting.Reporter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This is the primary class you should interact with when building a [KneatNetwork].
 * Allows you to create, reproduce, mutate, and select [KneatGenome]s (or essentially the blueprint to build a
 * neural network) to quickly find a solution to a problem domain.
 */
object Pipeline {

    /**
     * The coroutine scope used by the pipeline to execute _the evolutionary algorithm_ specifically.
     * Note that this context does _not_ apply to the same parallelization scheme used by a
     * [Network] to achieve parallel node/connection activation.
     */
    private val pipelineContext: CoroutineScope = CoroutineScope(
        context = SupervisorJob() +
                Dispatchers.IO
    )

    // ********* Algorithm Data

    /**
     * The current generation being executed
     */
    private var generation = 1L

    /**
     * The current species existing in our simulation
     */
    private var species: List<Species>? = null

    /**
     * The best genome we've seen so far
     */
    private var bestGenome: Genome? = null

    /**
     * The total population of genomes living in the simulation
     */
    private lateinit var population: Map<Long, List<Genome>>

    // ********* Configurable items

    /**
     * The configuration used to specify how the pipeline (genomes, stagnation, reproduction, speciation, etc.)
     * behaves.
     */
    private lateinit var config: Configuration

    /**
     * The function used to evaluate a [KneatGenome]'s fitness
     */
    private lateinit var evaluationFunction: EvaluationFunction

    /**
     * A list of listeners that wish to be updated about the state of the pipeline
     */
    private var stateListeners = mutableListOf<PipelineStateCallback>()

    /**
     * A list of reporters that wish to be updated about the state, status, and info of the pipeline
     */
    private var reporters = mutableListOf<Reporter>()

    /**
     * The stagnation scheme used by the pipeline
     */
    private var stagnation: StagnationScheme = KneatStagnationScheme()

    /**
     * The reproduction scheme used by the pipeline
     */
    private var reproduction: ReproductionScheme = KneatReproductionScheme()

    /**
     * The speciation scheme used by the pipeline
     */
    private var speciationScheme: SpeciationScheme = KneatSpeciationScheme()

    // ********* Internal state helpers

    /**
     * The current (mutable) state of the pipeline; should only be used internally to the pipeline
     */
    private val _state = MutableStateFlow<PipelineState>(PipelineState.Genesis)

    /**
     * Used to determine if the pipeline should pause after the current evaluation step has finished
     */
    private var shouldPause: AtomicBoolean = AtomicBoolean(false)

    /**
     * Used to determine if the pipeline should pause and save after the current evaluation step has finished
     */
    private var shouldSave: AtomicBoolean = AtomicBoolean(false)

    /**
     * Used to determine if the pipeline should pause if running and reload the pipeline from a saved file
     */
    private var shouldLoad: AtomicBoolean = AtomicBoolean(false)

    /**
     * The current state of the pipeline. May be subscribed to to get updates without adding a [PipelineStateCallback]
     * via [withStateListener].
     */
    val state: StateFlow<PipelineState> = _state

    // Configuration functions

    /**
     * Sets the [EvaluationFunction] of the pipeline; this will be the sole criteria used to judge your
     * [KneatGenome]'s fitnesses. This is required to be called; if not, the pipeline will throw an
     * [IllegalStateException].
     */
    fun withEvaluationFunction(evaluationFunction: EvaluationFunction) : Pipeline {
        Pipeline.evaluationFunction = evaluationFunction
        return this
    }

    /**
     * Adds a [Reporter] to the reporter list; there is no limit to the number of reporters you can add to a
     * pipeline.
     */
    fun withReporter(reporter: Reporter) : Pipeline {
        reporters.add(reporter)
        stateListeners.add(reporter)
        return this
    }

    /**
     * Adds a [PipelineStateCallback] state listener to get notified of pipeline updates; there is no limit
     * to the number of state listeners you can add to a pipeline.
     */
    fun withStateListener(stateListener: PipelineStateCallback) : Pipeline {
        stateListeners.add(stateListener)
        return this
    }

    /**
     * Sets the [Configuration] for the pipeline. This is required to be called; if not, the pipeline will throw an
     * [IllegalStateException].
     */
    fun withConfiguration(config: Configuration) : Pipeline {
        Pipeline.config = config
        return this
    }

    /**
     * Sets the reproduction scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatReproductionScheme] will be used by default.
     */
    fun withReproductionScheme(reproductionScheme: ReproductionScheme) : Pipeline {
        reproduction = reproductionScheme
        return this
    }

    /**
     * Sets the stagnation scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatStagnationScheme] will be used by default.
     */
    fun withStagnationScheme(stagnationScheme: StagnationScheme) : Pipeline {
        stagnation = stagnationScheme
        return this
    }

    /**
     * Sets the speciation scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatSpeciationScheme] will be used by default.
     */
    fun withSpeciationScheme(speciationScheme: SpeciationScheme) : Pipeline {
        Pipeline.speciationScheme = speciationScheme
        return this
    }

    // ******* Pipeline Management Functions

    /**
     * Starts the execution of the pipeline. This will run indefinitely until either:
     * 1. A solution has been found via the [TerminationCriterion] has been met
     * 2. An extinction event has occurred and [Configuration.resetOnExtinction] has been set to false
     * 3. [pause], [save], or [load] have been called explicitly by you
     * 4. An error occurs; in this case, the logs will be updated with what happened and hopefully how to address it
     *
     * *Note*: You must have called [withEvaluationFunction] and [withConfiguration] prior to calling this; otherwise
     * an [IllegalStateException] will be thrown.
     */
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
                is PipelineState.GenerationStarted -> {
                    evaluateCurrentGeneration(evaluationFunction)
                }
                is PipelineState.GenerationFinished -> {
                    reporters.report().info("Current best genome: ${bestGenome?.id} with fitness ${bestGenome?.getFitness()}")
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
                is PipelineState.Extinction -> {
                    if (config.resetOnExtinction) {
                        generation = 1
                        _state.emit(PipelineState.Genesis)
                    } else {
                        terminate()
                    }
                }
            }
        }

        return this
    }

    /**
     * Stops the current flow of the pipeline _after the current [PipelineState.ExecutionState] has finished_. For
     * example, if your pipeline is currently in the middle of a [PipelineState.ExecutionState.ReproducingPopulation]
     * and you call pause, it will finish reproducing the population and be paused once the pipeline is in the
     * [PipelineState.ReproducedPopulation] state.
     */
    fun pause() : Pipeline {
        shouldPause.set(true)
        return this
    }

    // Resumes the current flow of the pipeline. Used to restart after pause is called
    /**
     * Resumes the pipeline after a [pause] event has been triggered. If the pipeline had not
     * previously been paused, all reporters will be notified of a [Reporter.error] scenario; however,
     * the pipeline will continue doing what it was doing as to not ruin your progress if this is
     * accidentally called at an incorrect time.
     */
    suspend fun resume() = withContext(pipelineContext.coroutineContext) {
        val currentState = _state.value

        if (currentState !is PipelineState.ExecutionState.Paused) {
            reporters.report().error("Resume called while the pipeline was not paused!")
        } else {
            shouldPause.set(false)
            notify(currentState.resumeState)
        }

        return@withContext this@Pipeline
    }

    // Automatically pauses the pipeline if running
    /**
     * Loads the pipeline from a [file] to continue execution that was [save]d at a previous time.
     *
     * *WARNING!* If the pipeline is currently in progress and this method is called, the pipeline will pause
     * *all current information will be deleted and the pipeline will act as if it's resuming from the loaded state!*
     *
     * If you do not wish to lose all of your progress of a currently running pipeline, call [save] prior to this.
     *
     * TODO: This functionality currently does not exist; will be added in a future feature
     */
    suspend fun loadFrom(file: File) = withContext(pipelineContext.coroutineContext) {
        shouldLoad.set(true)
        //TODO: Need to store the file reference to use during the load function
        return@withContext this@Pipeline
    }

    /**
     * Saves the pipeline to a [file] to be resumed at a later time. The pipeline is automatically
     * paused during a [PipelineState.ExecutionState.Saving] event and *must be resumed manually* after
     * a [PipelineState.Saved] state has been posted, if wanted.
     */
    suspend fun saveTo(file: File) = withContext(pipelineContext.coroutineContext) {
        shouldSave.set(true)
        //TODO: Need to store the file reference to use during the save function
        return@withContext this@Pipeline
    }

    /**
     * Terminates the Pipeline without any consideration for what is current running.
     *
     * *Warning!* This will NOT wait for a current execution step to finish. This will NOT wait for a save
     * to be finished. Only call this method when you are sure you've saved or don't care about data loss.
     */
    fun terminate() {
        runBlocking {
            pipelineContext.coroutineContext.job.cancelAndJoin()
            cancel()
        }
    }

    // ****** Private pipeline management functions

    /**
     * Determines the next state depending on the different "pausable" states available in the pipeline.
     */
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

    /**
     * Does the heavy lifting of saving the pipeline to a file
     */
    private suspend fun save(resumeState: PipelineState) {
        notify(PipelineState.ExecutionState.Saving(resumeState))

        // TODO : Save the resume state as well so when we load, we can go to the next state

        notify(PipelineState.Saved(resumeState))
    }

    /**
     * Does the heavy lifting of loading a pipeline from a file
     */
    private suspend fun load() {
        notify(PipelineState.ExecutionState.Loading)

        //TODO: Get the state from the loaded pipeline
        val loadedResumeState = PipelineState.Genesis


        shouldPause.set(false)
        shouldSave.set(false)
        shouldLoad.set(false)
        notify(loadedResumeState)
    }

    /**
     * Creates the initial population via the [ReproductionScheme]
     */
    private suspend fun createInitialPopulation() {
        notify(PipelineState.ExecutionState.CreatingInitialPopulation)

        population = reproduction.create(
            config.genomeConfiguration,
            config.populationSize,
            reporters = reporters
        )

        notify(PipelineState.InitialPopulationCreated)
    }

    /**
     * Organizes the current population into species so that we can track important information like
     * if a population subset is stagnant, for example.
     */
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

    /**
     * Starts the next generation of the simulation
     */
    private suspend fun startNextGeneration() {
        generation++
        GenomeCache.clearCache()
        notify(PipelineState.GenerationStarted)
    }

    /**
     * Uses the [SpeciationScheme] to cull a population based on the criteria defined in [Configuration]
     */
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

    /**
     * Reproduces the current population to make the next generation of individuals
     */
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

    /**
     * Evaluates all [KneatGenome]s in the current population with the [evaluationFunction]
     */
    private suspend fun evaluateCurrentGeneration(evaluationFunction: EvaluationFunction) {
        species?.forEach {spec ->
            spec.members.forEach {
                notify(PipelineState.ExecutionState.Evaluating(spec.key, it))
                val fitness = it.evaluate(evaluationFunction, generation)
                var isSolution = false

                reporters.report().info("Evaluated genome ${it.id}; fitness was $fitness")

                val greatest = bestGenome
                if (greatest == null || fitness > greatest.getFitness()) {
                    bestGenome = it
                }

                config.terminationCriterion?.let { criteria ->
                    if (fitness > criteria.fitnessThreshold) {
                        isSolution = true
                    }
                }

                if (isSolution) {
                    notify(PipelineState.SolutionFound(it))
                    return
                }
            }
        }

        notify(PipelineState.GenerationFinished)
    }

    /**
     * Helper function to notify the state listeners and reporters of a pipeline change
     */
    private suspend fun notify(state: PipelineState) {
        _state.emit(state)
        stateListeners.forEach { it.notify(state) }
    }
}