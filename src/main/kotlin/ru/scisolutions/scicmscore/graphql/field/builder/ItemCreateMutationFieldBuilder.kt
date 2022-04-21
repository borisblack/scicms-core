package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.LocaleInputValueBuilder
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.MajorRevInputValueBuilder

class ItemCreateMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )

        if (item.versioned && item.manualVersioning)
            builder.inputValueDefinition(MajorRevInputValueBuilder(true).build())

        if (item.localized)
            builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}