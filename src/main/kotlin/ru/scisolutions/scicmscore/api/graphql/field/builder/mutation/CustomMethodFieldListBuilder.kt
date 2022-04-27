package ru.scisolutions.scicmscore.api.graphql.field.builder.mutation

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionListBuilder

class CustomMethodFieldListBuilder(private val itemName: String, private val customMethodNames: Set<String>) : FieldDefinitionListBuilder {
    override fun buildList(): List<FieldDefinition> {
        val fields = mutableListOf<FieldDefinition>()
        val capitalizedItemName = itemName.capitalize()
        for (methodName in customMethodNames) {
            fields.add(
                FieldDefinition.newFieldDefinition()
                    .name("${methodName}${capitalizedItemName}")
                    .type(TypeName("${capitalizedItemName}CustomMethodResponse"))
                    .inputValueDefinition(
                        InputValueDefinition.newInputValueDefinition()
                            .name("data")
                            .type(TypeNames.JSON)
                            .build()
                    )
                    .build()
            )
        }

        return fields.toList()
    }
}