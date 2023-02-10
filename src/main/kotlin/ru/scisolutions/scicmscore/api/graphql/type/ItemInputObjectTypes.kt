package ru.scisolutions.scicmscore.api.graphql.type

import graphql.language.EnumTypeDefinition
import graphql.language.EnumValueDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.upperFirst

@Component
class ItemInputObjectTypes(
    private val includeAttributePolicy: IncludeAttributePolicy,
    private val attributeTypes: AttributeTypes
) {
    fun filtersInput(item: Item): InputObjectTypeDefinition {
        val inputName = "${item.name.upperFirst()}FiltersInput"
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
            .filter { (attrName, attribute) -> includeAttributePolicy.includeInFiltersInputObjectType(item, attrName, attribute) }
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
            .name("${item.name.upperFirst()}Input")

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> includeAttributePolicy.includeInInputObjectType(item, attrName, attribute) }
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
            .filter { (_, attribute) -> attribute.type == FieldType.enum }
            .map { (attrName, attribute) ->
                enumType(item, attrName, attribute)
            }

    private fun enumType(item: Item, attrName: String, attribute: Attribute): EnumTypeDefinition {
        if (attribute.type != FieldType.enum)
            throw IllegalArgumentException("Attribute [$attrName] is not enumeration.")

        if (attribute.enumSet.isNullOrEmpty())
            throw IllegalArgumentException("Attribute [$attrName] enumeration set is null or empty.")

        return EnumTypeDefinition.newEnumTypeDefinition()
            .name("${item.name.upperFirst()}${attrName.upperFirst()}Enum")
            .enumValueDefinitions(
                attribute.enumSet.map {
                    EnumValueDefinition.newEnumValueDefinition().name(it).build()
                }
            )
            .build()
    }
}