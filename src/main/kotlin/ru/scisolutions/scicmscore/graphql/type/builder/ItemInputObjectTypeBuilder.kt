package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.InputObjectTypeDefinition
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.AttributeInputValueBuilder

class ItemInputObjectTypeBuilder(private val item: Item) {
    fun build(): InputObjectTypeDefinition {
        val builder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromFiltersInputObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.inputValueDefinition(
                    AttributeInputValueBuilder(attrName, attribute).build()
                )
            }

        return builder.build()
    }

    companion object {
        private val excludeAttributePolicy = ExcludeAttributePolicy()
    }
}