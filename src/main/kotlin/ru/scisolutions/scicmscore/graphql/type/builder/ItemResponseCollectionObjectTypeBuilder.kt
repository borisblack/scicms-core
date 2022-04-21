package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemResponseCollectionObjectTypeBuilder(private val item: Item) {
    fun build(): ObjectTypeDefinition {
        val capitalizedItemName = item.name.capitalize()
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
}