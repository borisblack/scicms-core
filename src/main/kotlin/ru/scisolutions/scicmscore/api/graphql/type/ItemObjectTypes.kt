package ru.scisolutions.scicmscore.api.graphql.type

import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.extension.upperFirst

@Component
class ItemObjectTypes(
    private val attributeTypes: AttributeTypes,
    private val includeAttributePolicy: IncludeAttributePolicy
) {
    fun item(item: Item): ObjectTypeDefinition {
        val dataSourceInfo = "Data source: ${item.ds}."
        var description = item.description?.trim()
        description =
            if (description == null) {
                dataSourceInfo
            } else {
                val separator = if (description.endsWith(".")) " " else ". "
                "${description}${separator}$dataSourceInfo"
            }

        val builder =
            ObjectTypeDefinition.newObjectTypeDefinition()
                .name(item.name.upperFirst())
                .description(Description(description, null, true))

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> includeAttributePolicy.includeInObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.fieldDefinition(
                    newAttributeField(item, attrName, attribute)
                )
            }

        return builder.build()
    }

    private fun newAttributeField(item: Item, attrName: String, attribute: Attribute): FieldDefinition {
        val builder =
            FieldDefinition.newFieldDefinition()
                .name(attrName)
                .description(Description(attribute.description?.trim(), null, true))
                .type(attributeTypes.objectType(item, attrName, attribute))

        if (attribute.isCollection()) {
            requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null." }

            val capitalizedTargetItemName = attribute.target.upperFirst()

            builder
                .inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name("filters")
                        .type(TypeName("${capitalizedTargetItemName}FiltersInput"))
                        .build()
                )
                .inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name("pagination")
                        .type(TypeName("PaginationInput"))
                        .build()
                )
                .inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name("sort")
                        .type(ListType(TypeNames.STRING))
                        .build()
                )
        }

        return builder.build()
    }

    fun response(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}Response")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(TypeName(capitalizedItemName))
                    .build()
            )
            .build()
    }

    fun relationResponse(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}RelationResponse")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(TypeName(capitalizedItemName))
                    .build()
            )
            .build()
    }

    fun flaggedResponse(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}FlaggedResponse")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("success")
                    .type(TypeNames.BOOLEAN)
                    .build()
            )
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(TypeName(capitalizedItemName))
                    .build()
            )
            .build()
    }

    fun responseCollection(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}ResponseCollection")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(
                        NonNullType(
                            ListType(
                                NonNullType(TypeName(capitalizedItemName))
                            )
                        )
                    )
                    .build()
            )
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("meta")
                    .type(NonNullType(TypeName("ResponseCollectionMeta")))
                    .build()
            )
            .build()
    }

    fun relationResponseCollection(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}RelationResponseCollection")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(
                        NonNullType(
                            ListType(
                                NonNullType(TypeName(capitalizedItemName))
                            )
                        )
                    )
                    .build()
            )
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("meta")
                    .type(NonNullType(TypeName("ResponseCollectionMeta")))
                    .build()
            )
            .build()
    }

    fun customMethodResponse(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.upperFirst()

        return ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${capitalizedItemName}CustomMethodResponse")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(TypeNames.OBJECT)
                    .build()
            )
            .build()
    }
}
