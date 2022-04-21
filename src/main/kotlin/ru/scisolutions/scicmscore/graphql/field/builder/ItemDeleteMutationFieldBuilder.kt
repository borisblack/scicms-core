package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.TypeNames

class ItemDeleteMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("delete${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeNames.ID))
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