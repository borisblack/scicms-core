package ru.scisolutions.scicmscore.api.graphql.type

import graphql.language.EnumTypeDefinition
import graphql.language.EnumValueDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ItemInputObjectTypes(
    private val excludeAttributePolicy: ExcludeAttributePolicy,
    private val attributeTypes: AttributeTypes
) {
    fun filtersInput(item: Item): InputObjectTypeDefinition {
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
                        .type(attributeTypes.filterInputType(item, attrName, attribute))
                        .build()
                )
            }

        return builder.build()
    }

    fun itemInput(item: Item): InputObjectTypeDefinition {
        val builder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromInputObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(attributeTypes.inputType(item, attrName, attribute))
                        .build()

                )
            }

        return builder.build()
    }

    fun enumTypes(item: Item): List<EnumTypeDefinition> =
        item.spec.attributes
            .filter { (_, attribute) -> attribute.type == Type.enum }
            .map { (attrName, attribute) ->
                enumType(item, attrName, attribute)
            }

    private fun enumType(item: Item, attrName: String, attribute: Attribute): EnumTypeDefinition {
        if (attribute.type != Type.enum)
            throw IllegalArgumentException("Attribute [$attrName] is not enumeration.")

        if (attribute.enumSet.isNullOrEmpty())
            throw IllegalArgumentException("Attribute [$attrName] enumeration set is null or empty.")

        return EnumTypeDefinition.newEnumTypeDefinition()
            .name("${item.name.capitalize()}${attrName.capitalize()}Enum")
            .enumValueDefinitions(
                attribute.enumSet.map {
                    EnumValueDefinition.newEnumValueDefinition().name(it).build()
                }
            )
            .build()
    }
}