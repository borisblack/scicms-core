package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemResponseObjectTypeBuilder(private val item: Item) {
    fun build(): ObjectTypeDefinition {
        val capitalizedItemName = item.name.capitalize()
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
}