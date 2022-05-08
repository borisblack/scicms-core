package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.persistence.entity.Item

class RelationResponseObjectTypeBuilder : ObjectTypeBuilder {
    override fun fromItem(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.capitalize()
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
}