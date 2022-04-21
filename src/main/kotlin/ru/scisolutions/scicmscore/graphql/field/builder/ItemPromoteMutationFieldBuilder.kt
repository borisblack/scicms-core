package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemPromoteMutationFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        val capitalizedName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("promote${capitalizedName}")
            .type(TypeName("${capitalizedName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("state")
                    .type(NonNullType(TypeName("String")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}