package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeResolver
import ru.scisolutions.scicmscore.api.graphql.type.builder.ExcludeAttributePolicy
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ItemInputObjectTypeBuilder(
    private val typeResolver: TypeResolver,
    private val excludeAttributePolicy: ExcludeAttributePolicy
) : InputObjectTypeBuilder {
    override fun fromItem(item: Item): InputObjectTypeDefinition {
        val builder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        item.spec.attributes.asSequence()
            .filter { (attrName, _) -> excludeAttributePolicy.excludeFromInputObjectType(item, attrName) }
            .forEach { (attrName, attribute) ->
                builder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(typeResolver.inputType(item, attrName))
                        .build()

                )
            }

        return builder.build()
    }
}