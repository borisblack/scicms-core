package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.TypeName

class JsonMutationFieldBuilder(private val fieldName: String) {
    fun build(): FieldDefinition =
        FieldDefinition.newFieldDefinition()
            .name(fieldName)
            .type(TypeName("JSON"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(TypeName("JSON"))
                    .build()
            )
            .build()
}