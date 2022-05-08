package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.TypeResolver
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ItemObjectTypeBuilder(
    private val typeResolver: TypeResolver,
    private val excludeAttributePolicy: ExcludeAttributePolicy
) : ObjectTypeBuilder {
    override fun fromItem(item: Item): ObjectTypeDefinition {
        val builder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        item.spec.attributes.asSequence()
            .filter { (attrName, _) -> excludeAttributePolicy.excludeFromObjectType(item, attrName) }
            .forEach { (attrName, attribute) ->
                builder.fieldDefinition(
                    newAttributeField(item, attrName, attribute)
                )
            }

        return builder.build()
    }

    private fun newAttributeField(item: Item, attrName: String, attribute: Attribute): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(attrName)
            .type(typeResolver.objectType(item, attrName))

        if (attribute.isCollection()) {
            requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

            val capitalizedTargetItemName = attribute.target.capitalize()

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
}