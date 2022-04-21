package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.TypeNames
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.LocaleInputValueBuilder

class ItemCreateLocalizationMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        if (!item.localized)
            throw IllegalArgumentException("Item [${item.name}] is not localized. CreateLocalization mutation cannot be applied")

        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}Localization")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )

        builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}