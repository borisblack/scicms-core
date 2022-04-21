package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemResponseQueryFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.capitalize()}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.versioned)
        //     builder.inputValueDefinition(MajorRevInputValueBuilder().build())
        //
        // if (item.localized)
        //     builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}