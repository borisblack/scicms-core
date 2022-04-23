package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ResponseObjectTypeBuilder(private val item: Item) : ObjectTypeBuilder {
    override fun build(): ObjectTypeDefinition {
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