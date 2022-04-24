package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.engine.schema.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.TypeResolver

class ItemObjectTypeBuilder(private val item: Item) : ObjectTypeBuilder {
    override fun build(): ObjectTypeDefinition {
        val builder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                builder.fieldDefinition(
                    newAttributeField(attrName, attribute)
                )
            }

        return builder.build()
    }

    private fun newAttributeField(attrName: String, attribute: Attribute): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(attrName)
            .type(typeResolver.objectType(attrName, attribute))

        if (attribute.type == Attribute.Type.RELATION.value &&
            (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
        ) {
            requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

            val capitalizedTargetItemName = attribute.target.substringBefore("(").capitalize()

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

    companion object {
        private val excludeAttributePolicy = ExcludeAttributePolicy()
        private val typeResolver = TypeResolver()
    }
}