package kneat.evolution.species

import kneat.evolution.genome.Genome
import kneat.evolution.network.Aggregation
import kneat.util.report
import kneat.util.reporting.Reporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

open class KneatSpeciationScheme : SpeciationScheme {

    private val speciationScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    override suspend fun speciate(
        compatibilityThreshold: Float,
        population: Map<Long, List<Genome>>,
        generation: Long,
        fitnessAggregationFunction: Aggregation,
        reporters: MutableList<Reporter>,
        existingSpecies: List<Species>?
    ) : List<Species> = withContext(speciationScope.coroutineContext) {
        val newReps = existingSpecies?.let { getNewRepresentatives(it, population, compatibilityThreshold).toMutableList() } ?: mutableListOf()
        val existingSpeciesIdMap = newReps.map { it.first }

        val changedSpecies = existingSpecies?.filter { !existingSpeciesIdMap.contains(it.key) }

        changedSpecies?.forEach {
            //TODO: Move this into strings
            reporters.report().info("Species ${it.key} has changed beyond recognition; a new species will be discovered to take its place")
        }

        val speciesMap: MutableMap<Long, List<Genome>> = mutableMapOf()
        val unspeciated: MutableList<Genome> = mutableListOf()
        val repMap = newReps.toMap()

        // First pass;
        // Since most individuals generally won't deviate too much from their species, most should already
        // Be attached to their current rep. We want to avoid new speciation as much as possible
        // since it can be expensive
        population.forEach { (existingSpeciesId, genomeList) ->
            val rep = repMap[existingSpeciesId]

            if (rep == null) {
                unspeciated += genomeList
            } else {
                genomeList.filter { it.id != rep.id }.forEach { genome ->
                    val distance = rep.distance(genome)

                    if (distance < compatibilityThreshold) {
                        speciesMap[rep.id] = (speciesMap[rep.id] ?: emptyList()) + genome
                    } else {
                        unspeciated += genome
                    }
                }
            }
        }

        var maxSpeciesIndex = (existingSpecies?.maxOf { it.key } ?: 0) + 1

        unspeciated.forEach { currentIndividual ->
            var foundFamily = false

            newReps.forEach rep@ { (speciesId, representative) ->
                val distance = representative.distance(currentIndividual)

                if (distance < compatibilityThreshold) {
                    foundFamily = true
                    speciesMap[speciesId] = (speciesMap[speciesId] ?: emptyList()) + currentIndividual
                    return@rep
                }
            }

            if (!foundFamily) {
                reporters.report().info("A new species has been discovered!")
                newReps.add(Pair(maxSpeciesIndex++, currentIndividual))
            }
        }

        return@withContext newReps.map {
            val speciesKey = it.first
            val spec = existingSpecies?.find { spec -> spec.key == speciesKey }
            val rep = it.second
            val members = (speciesMap[speciesKey] ?: emptyList()) + rep

            if (spec == null) {
                KneatSpecies(
                    key = speciesKey,
                    createdIn = generation,
                    fitnessAggregationFunction = fitnessAggregationFunction,
                    representative = rep,
                    members = members.toMutableList()
                )
            } else {
                spec.update(
                    rep,
                    members
                )
            }
        }
    }

    private suspend fun getNewRepresentatives(
        existingSpecies: List<Species>,
        population: Map<Long, List<Genome>>,
        compatibilityThreshold: Float
    ) : List<Pair<Long, Genome>> {
        return existingSpecies.mapNotNull { species ->
            var newRepDist = compatibilityThreshold
            var newRep: Genome? = null
            (population[species.key] ?: emptyList()).forEach { genome ->
                val dist = species.representative.distance(genome)
                if (dist < newRepDist) {
                    newRep = genome
                    newRepDist = dist
                }
            }

            val finalizedRep = newRep
            if (finalizedRep != null) Pair(species.key, finalizedRep) else null
        }
    }
}