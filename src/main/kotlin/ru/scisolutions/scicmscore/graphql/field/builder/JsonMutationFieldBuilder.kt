package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import ru.scisolutions.scicmscore.graphql.TypeNames

class JsonMutationFieldBuilder(private val fieldName: String) {
    fun build(): FieldDefinition =
        FieldDefinition.newFieldDefinition()
            .name(fieldName)
            .type(TypeNames.JSON)
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(TypeNames.JSON)
                    .build()
            )
            .build()
}