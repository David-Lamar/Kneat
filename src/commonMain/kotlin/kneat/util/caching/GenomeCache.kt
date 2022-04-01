package kneat.util.caching

object GenomeCache {

    /**
     * Distances between two genomes can be calculated multiple times for the same two genomes during
     * different stages of the NEAT algorithm. Since executing the [Genome.distance] method is fairly expensive,
     * caching distances between genomes allows us to pull from the cache instead of recalculating distance each time
     * a duplicate call is made to [Genome.distance].
     */
    private var cachedDistances: MutableMap<Pair<Long, Long>, Float> = mutableMapOf()

    /**
     * Meta data about the cache for us to know how many "hits" or successful cache accesses we're seeing throughout execution
     */
    private var cacheHits: Long = 0

    /**
     * Meta data about the cache for us to know how many "misses" or times where the cache does not have a value
     * present we're seeing throughout execution
     */
    private var cacheMisses: Long = 0


    fun getMisses(): Long = cacheMisses

    fun getHits(): Long = cacheHits

    fun clearCache() {
        cachedDistances.clear()
        cacheHits = 0
        cacheMisses = 0
    }

    fun getFromCache(from: Long, to: Long): Float? {
        val distance = cachedDistances[Pair(from, to)]

        if (distance != null) cacheHits++ else cacheMisses++

        return distance
    }

    fun addToCache(from: Long, to: Long, distance: Float) {
        cachedDistances[Pair(from, to)] = distance
        cachedDistances[Pair(to, from)] = distance
    }
}