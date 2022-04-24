package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.api.graphql.TypeResolver
import ru.scisolutions.scicmscore.api.graphql.type.builder.ExcludeAttributePolicy

class FiltersInputObjectTypeBuilder(private val item: Item) : InputObjectTypeBuilder {
    override fun build(): InputObjectTypeDefinition {
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
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(typeResolver.filterInputType(attrName, attribute))
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