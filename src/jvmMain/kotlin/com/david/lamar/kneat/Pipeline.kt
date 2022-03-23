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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This is the primary class you should interact with when building a [com.david.lamar.kneat.genome.network.KneatNetwork].
 * Allows you to create, reproduce, mutate, and select [Genome]s (or essentially the blueprint to build a
 * neural network) to quickly find a solution to a problem domain.
 */
object Pipeline {

    /**
     * The coroutine scope used by the pipeline to execute _the evolutionary algorithm_ specifically.
     * Note that this context does _not_ apply to the same parallelization scheme used by a
     * [com.david.lamar.kneat.genome.network.Network] to achieve parallel node/connection activation.
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
    private lateinit var population: List<Genome>

    // ********* Configurable items

    /**
     * The configuration used to specify how the pipeline (genomes, stagnation, reproduction, speciation, etc.)
     * behaves.
     */
    private lateinit var config: Configuration

    /**
     * The function used to evaluate a [Genome]'s fitness
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
     * [Genome]'s fitnesses. This is required to be called; if not, the pipeline will throw an
     * [IllegalStateException].
     */
    fun withEvaluationFunction(evaluationFunction: EvaluationFunction) : Pipeline {
        this.evaluationFunction = evaluationFunction
        return this
    }

    /**
     * Adds a [Reporter] to the reporter list; there is no limit to the number of reporters you can add to a
     * pipeline.
     */
    fun withReporter(reporter: Reporter) : Pipeline {
        this.reporters.add(reporter)
        this.stateListeners.add(reporter)
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
        this.config = config
        return this
    }

    /**
     * Sets the reproduction scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatReproductionScheme] will be used by default.
     */
    fun withReproductionScheme(reproductionScheme: ReproductionScheme) : Pipeline {
        this.reproduction = reproductionScheme
        return this
    }

    /**
     * Sets the stagnation scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatStagnationScheme] will be used by default.
     */
    fun withStagnationScheme(stagnationScheme: StagnationScheme) : Pipeline {
        this.stagnation = stagnationScheme
        return this
    }

    /**
     * Sets the speciation scheme of the pipeline if you wish to provide your own; this is not required and a
     * [KneatSpeciationScheme] will be used by default.
     */
    fun withSpeciationScheme(speciationScheme: SpeciationScheme) : Pipeline {
        this.speciationScheme = speciationScheme
        return this
    }

    // ******* Pipeline Management Functions

    /**
     * Starts the execution of the pipeline. This will run indefinitely until either:
     * 1. A solution has been found via the [com.david.lamar.kneat.configuration.TerminationCriterion] has been met
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
     * Evaluates all [Genome]s in the current population with the [evaluationFunction]
     */
    private suspend fun evaluateCurrentGeneration(evaluationFunction: EvaluationFunction) {
        //TODO: This can be parallelized!
        population.forEach {
            notify(PipelineState.ExecutionState.Evaluating(it))
            it.evaluate(evaluationFunction, generation)
            reporters.report().info("Evaluated genome ${it.key}; fitness was ${it.getFitness(generation)}")
        }

        notify(PipelineState.Evaluated)
    }

    /**
     * Collects meta data about the current generation such as the best overall genome we've seen, if a solution
     * has been found, etc.
     */
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

    /**
     * Helper function to notify the state listeners and reporters of a pipeline change
     */
    private suspend fun notify(state: PipelineState) {
        _state.emit(state)
        stateListeners.forEach { it.notify(state) }
    }

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

    /**
     * Callback to receive different [PipelineState] updates; these functions have a 1-1 mapping
     * to the different [PipelineState]s.
     */
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

        /**
         * Helper function to notify a [PipelineStateCallback] when a specific [PipelineState] has been
         * emitted
         */
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