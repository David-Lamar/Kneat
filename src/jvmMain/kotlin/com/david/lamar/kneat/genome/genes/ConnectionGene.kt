package com.david.lamar.kneat.genome.genes

import com.david.lamar.kneat.configuration.GenomeConfiguration
import com.david.lamar.kneat.genome.genes.attributes.Attribute
import com.david.lamar.kneat.genome.genes.attributes.BooleanAttribute
import com.david.lamar.kneat.genome.genes.attributes.FloatAttribute
import kotlin.math.abs

class ConnectionGene(
    private val weightConfiguration: GenomeConfiguration.WeightConfiguration,
    private val connectionConfiguration: GenomeConfiguration.ConnectionConfiguration,
    attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>? = null
) : Gene() {

    override val managedAttributes = attributes ?: mapOf(
        WEIGHT_GENE to FloatAttribute(weightConfiguration),
        ENABLED_GENE to BooleanAttribute(connectionConfiguration)
    )

    override fun distance(other: Gene): Float {
        val myWeight = managedAttributes.getAs<FloatAttribute>(WEIGHT_GENE)
        val otherWeight = other.managedAttributes.getAs<FloatAttribute>(WEIGHT_GENE)

        val myEnabled = managedAttributes.getAs<BooleanAttribute>(ENABLED_GENE)
        val otherEnabled = other.managedAttributes.getAs<BooleanAttribute>(ENABLED_GENE)

        var delta = abs(myWeight.value - otherWeight.value)
        if (myEnabled.value != otherEnabled.value) delta += 1f

        return delta
    }

    override fun copy(
        attributes: Map<String, Attribute<out Any, out GenomeConfiguration.AttributeConfiguration<out Any>>>
    ): Gene {
        return ConnectionGene(
            weightConfiguration = weightConfiguration,
            connectionConfiguration = connectionConfiguration,
            attributes = attributes
        )
    }

    companion object {
        const val WEIGHT_GENE = "weight"
        const val ENABLED_GENE = "enabled"
    }
}