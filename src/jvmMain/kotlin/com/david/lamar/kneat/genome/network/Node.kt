package com.david.lamar.kneat.genome.network

import com.david.lamar.kneat.genome.genes.NodeGene
import com.david.lamar.kneat.genome.genes.NodeGene.Companion.ACTIVATION_GENE
import com.david.lamar.kneat.genome.genes.NodeGene.Companion.AGGREGATION_GENE
import com.david.lamar.kneat.genome.genes.NodeGene.Companion.BIAS_GENE
import com.david.lamar.kneat.genome.genes.NodeGene.Companion.RESPONSE_GENE
import com.david.lamar.kneat.genome.genes.attributes.ActivationAttribute
import com.david.lamar.kneat.genome.genes.attributes.AggregationAttribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import com.david.lamar.kneat.genome.genes.getAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Node(
    override val id: Long,
    override val gene: NodeGene
) : NetworkElement<Long, NodeGene>() {

    private val nodeContext: CoroutineScope = CoroutineScope(
        context = SupervisorJob() +
                Dispatchers.IO
    )

    private val _valueFlow = MutableStateFlow(0f)
    val valueFlow: StateFlow<Float> = _valueFlow

    private suspend fun activate(values: List<Float>) {
        val aggregation = gene.managedAttributes.getAs<AggregationAttribute>(AGGREGATION_GENE).value
        val activation = gene.managedAttributes.getAs<ActivationAttribute>(ACTIVATION_GENE).value
        val response = gene.managedAttributes.getAs<FloatAttribute>(RESPONSE_GENE).value
        val bias = gene.managedAttributes.getAs<FloatAttribute>(BIAS_GENE).value
        val aggregated = aggregation.aggregate(values)

        _valueFlow.emit(activation.activate(response + aggregated * bias))
    }

    fun registerTrigger(trigger: StateFlow<List<Float>>) {
        nodeContext.launch {
            trigger.collect {
                activate(it)
            }
        }
    }
}