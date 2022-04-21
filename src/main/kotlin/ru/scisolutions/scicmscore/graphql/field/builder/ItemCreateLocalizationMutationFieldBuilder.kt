package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.LocaleInputValueBuilder

class ItemCreateLocalizationMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        if (!item.localized)
            throw IllegalArgumentException("Item [${item.name}] is not localized. CreateLocalization mutation cannot be applied")

        val capitalizedName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedName}Localization")
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

        builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}