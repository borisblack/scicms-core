package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import ru.scisolutions.scicmscore.api.graphql.TypeResolver
import ru.scisolutions.scicmscore.api.graphql.type.builder.ExcludeAttributePolicy
import ru.scisolutions.scicmscore.persistence.entity.Item

class ItemInputObjectTypeBuilder(private val item: Item) : InputObjectTypeBuilder {
    override fun build(): InputObjectTypeDefinition {
        val builder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        item.spec.attributes.asSequence()
            .filter { (attrName, _) -> excludeAttributePolicy.excludeFromInputObjectType(item, attrName) }
            .forEach { (attrName, attribute) ->
                builder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(typeResolver.inputType(attrName, attribute))
                        .build()

                )
            }

        return builder.build()
    }

    companion object {
        private val excludeAttributePolicy = ExcludeAttributePolicy()
        private val typeResolver = TypeResolver()
    }
}