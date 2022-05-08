package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.persistence.entity.Item

class CustomMethodResponseObjectTypeBuilder : ObjectTypeBuilder {
    override fun fromItem(item: Item): ObjectTypeDefinition {
        val capitalizedItemName = item.name.capitalize()

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