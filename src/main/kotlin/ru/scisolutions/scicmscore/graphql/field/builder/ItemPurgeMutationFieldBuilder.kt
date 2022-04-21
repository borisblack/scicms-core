package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.TypeNames

class ItemPurgeMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        if (!item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is not versioned. Purge mutation cannot be applied")

        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("purge${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}