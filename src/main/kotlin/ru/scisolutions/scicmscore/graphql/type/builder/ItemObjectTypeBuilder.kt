package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.Description
import graphql.language.ObjectTypeDefinition
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.field.builder.AttributeFieldBuilder

class ItemObjectTypeBuilder(private val item: Item) {
    fun build(): ObjectTypeDefinition {
        val builder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.fieldDefinition(
                    AttributeFieldBuilder(attrName, attribute).build()
                )
            }

        return builder.build()
    }

    companion object {
        private val excludeAttributePolicy = ExcludeAttributePolicy()
    }
}