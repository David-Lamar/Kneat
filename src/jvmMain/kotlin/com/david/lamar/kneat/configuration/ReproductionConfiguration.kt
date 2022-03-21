package com.david.lamar.kneat.configuration

data class ReproductionConfiguration(
    val elitism: Int = 0,
    val survivalThreshold: Float = .2f,
    val minSpeciesSize: Int = 2
)