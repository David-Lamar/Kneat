package com.david.lamar.kneat.genome.network

interface Network {
    suspend fun activate(inputs: List<Float>, stabilizationDelay: Long) : List<Float>
    fun reset()
}