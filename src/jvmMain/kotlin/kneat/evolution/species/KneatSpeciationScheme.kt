package kneat.evolution.species

import kneat.evolution.genome.Genome
import kneat.evolution.network.Aggregation
import kneat.util.mapParallel
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
        population: List<Genome>,
        generation: Long,
        fitnessAggregationFunction: Aggregation,
        reporters: MutableList<Reporter>,
        existingSpecies: List<Species>?
    ) : List<Species> = withContext(speciationScope.coroutineContext) {
        val newReps = existingSpecies?.let { getNewRepresentatives(it, population).toMutableList() } ?: mutableListOf()
        val speciesMap: MutableMap<Long, List<Genome>> = mutableMapOf()

        // TODO: This is expensive but hard to parallelize. Since the new species items need to modify the
        //  representatives but currently un-indexed items need to reference the representatives, we could get in
        //  a scenario where an item is determined a new rep, and another item, in parallel, is determined a new
        //  rep, but really they're in the same "species"
        val unspeciated = population.filter { genome -> !newReps.any { it.second == genome } }
        unspeciated.forEach { currentIndividual ->
            var foundFamily = false

            newReps.forEach rep@ { representative ->
                val distance = representative.second.distance(currentIndividual)

                if (distance < compatibilityThreshold) {
                    foundFamily = true
                    speciesMap[representative.second.id] = (speciesMap[representative.second.id] ?: emptyList()) + currentIndividual
                    return@rep
                }
            }

            if (!foundFamily) { // New Species!!
                newReps.add(Pair(null, currentIndividual))
            }
        }

        var maxSpeciesIndex = (existingSpecies?.maxOf { it.key } ?: 0) + 1

        return@withContext newReps.mapNotNull { rep ->
            val speciesKey = rep.first
            val members = (speciesMap[rep.second.id] ?: emptyList()) + rep.second

            if (speciesKey == null) {
                KneatSpecies(
                    key = maxSpeciesIndex++,
                    createdIn = generation,
                    fitnessAggregationFunction = fitnessAggregationFunction,
                    representative = rep.second,
                    members = members.toMutableList()
                )
            } else {
                val existing = existingSpecies?.find { spec -> spec.key == speciesKey }
                existing?.update(rep.second, members)
            }
        }
    }

    private suspend fun getNewRepresentatives(
        existingSpecies: List<Species>,
        population: List<Genome>,
    ) : List<Pair<Long?, Genome>> {
        return existingSpecies.mapParallel { species ->
            var newRepDist = Float.MAX_VALUE
            var newRep: Genome = population.first()
            population.forEach { genome ->
                val dist = species.representative.distance(genome)
                if (dist < newRepDist) {
                    newRep = genome
                    newRepDist = dist
                }
            }

            Pair(species.key, newRep)
        }
    }
}