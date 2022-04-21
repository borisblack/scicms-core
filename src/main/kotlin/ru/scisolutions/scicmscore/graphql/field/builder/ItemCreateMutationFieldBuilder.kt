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
        val capitalizedName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedName}")
            .type(TypeName("${capitalizedName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${capitalizedName}Input")))
                    .build()
            )

        if (item.versioned && item.manualVersioning)
            builder.inputValueDefinition(MajorRevInputValueBuilder(true).build())

        if (item.localized)
            builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}