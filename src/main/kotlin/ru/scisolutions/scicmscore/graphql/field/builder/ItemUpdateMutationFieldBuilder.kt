package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemUpdateMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        if (item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is versioned. Update mutation cannot be applied")

        val capitalizedName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("update${capitalizedName}")
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

        // if (item.localized)
        //     builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}