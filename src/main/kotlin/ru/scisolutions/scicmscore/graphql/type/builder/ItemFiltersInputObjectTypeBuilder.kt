package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.AttributeFilterInputValueBuilder

class ItemFiltersInputObjectTypeBuilder(private val item: Item) {
    fun build(): InputObjectTypeDefinition {
        val inputName = "${item.name.capitalize()}FiltersInput"
        val builder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name(inputName)
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("and")
                    .type(ListType(TypeName(inputName)))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("or")
                    .type(ListType(TypeName(inputName)))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("not")
                    .type(TypeName(inputName))
                    .build()
            )

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromFiltersInputObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.inputValueDefinition(
                    AttributeFilterInputValueBuilder(attrName, attribute).build()
                )
            }

        return builder.build()
    }

    companion object {
        private val excludeAttributePolicy = ExcludeAttributePolicy()
    }
}