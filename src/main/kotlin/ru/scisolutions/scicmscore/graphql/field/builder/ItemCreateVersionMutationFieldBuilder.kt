package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.LocaleInputValueBuilder
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.MajorRevInputValueBuilder

class ItemCreateVersionMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        if (!item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is not versioned. CreateVersion mutation cannot be applied")

        val capitalizedName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedName}Version")
            .type(TypeName("${capitalizedName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${capitalizedName}Input")))
                    .build()
            )

        if (item.manualVersioning)
            builder.inputValueDefinition(MajorRevInputValueBuilder(true).build())

        if (item.localized)
            builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}