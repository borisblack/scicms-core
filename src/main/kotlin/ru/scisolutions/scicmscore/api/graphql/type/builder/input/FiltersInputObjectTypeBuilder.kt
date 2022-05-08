package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeResolver
import ru.scisolutions.scicmscore.api.graphql.type.builder.ExcludeAttributePolicy
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class FiltersInputObjectTypeBuilder(
    private val excludeAttributePolicy: ExcludeAttributePolicy,
    private val typeResolver: TypeResolver
) : InputObjectTypeBuilder {
    override fun fromItem(item: Item): InputObjectTypeDefinition {
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
            .filter { (attrName, _) -> excludeAttributePolicy.excludeFromFiltersInputObjectType(item, attrName) }
            .forEach { (attrName, _) ->
                builder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(typeResolver.filterInputType(item, attrName))
                        .build()
                )
            }

        return builder.build()
    }
}