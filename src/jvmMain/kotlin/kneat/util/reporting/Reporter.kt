package kneat.util.reporting

import kneat.Pipeline

/**
 * Provides a way for a developer to get information about the solution being generated while
 * execution is happening. For larger networks and problem domains, it's good to have periodic updates
 * to know that your solution is still being worked on even though it may be taking a long time.
 *
 * Aside from the additional functions you see here, it also has tie-ins to the [Pipeline.PipelineState] of
 * the currently executing pipeline.
 */
interface Reporter : Pipeline.PipelineStateCallback {

    /**
     * Reports general info about the solution being generated as it progresses
     */
    fun info(info: String)

    /**
     * Reports warnings about the solution being generated as it progresses
     */
    fun warn(warning: String)

    /**
     * Reports an error in the event of a crash, configuration issue, or an otherwise pipeline terminating
     * event
     */
    fun error(error: String, throwable: Throwable? = null)
    //fun onSpeciesStagnant(species: Species) TODO: Re-add this?
}

