package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.persistence.entity.Item

class RelationResponseCollectionObjectTypeBuilder(private val item: Item) : ObjectTypeBuilder {
    override fun build(): ObjectTypeDefinition {
        val capitalizedItemName = item.name.capitalize()
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
            .build()
    }
}