package com.david.lamar.kneat.genome.network

import com.david.lamar.kneat.genome.genes.ConnectionGene
import com.david.lamar.kneat.genome.genes.attributes.BooleanAttribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import com.david.lamar.kneat.genome.genes.getAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Connection(
    override val id: Pair<Long, Long>,
    override val gene: ConnectionGene
) : NetworkElement<Pair<Long, Long>, ConnectionGene>() {
    private val connectionScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    private val _valueFlow = MutableStateFlow(0f)
    val valueFlow: StateFlow<Float> = _valueFlow

    private suspend fun activate(value: Float) {
        val weight = gene.managedAttributes.getAs<FloatAttribute>(ConnectionGene.WEIGHT_GENE).value
        val enabled = gene.managedAttributes.getAs<BooleanAttribute>(ConnectionGene.ENABLED_GENE).value
        _valueFlow.emit(if (enabled) value * weight else 0f)
    }

    fun registerTrigger(trigger: StateFlow<Float>) {
        connectionScope.launch {
            trigger.collect { activate(it) }
        }
    }
}
