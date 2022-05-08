package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.persistence.entity.Item

class ResponseCollectionObjectTypeBuilder : ObjectTypeBuilder {
    override fun fromItem(item: Item): ObjectTypeDefinition {
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