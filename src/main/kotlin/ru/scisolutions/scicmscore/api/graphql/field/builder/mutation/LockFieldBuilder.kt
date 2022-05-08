package ru.scisolutions.scicmscore.api.graphql.field.builder.mutation

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionBuilder
import ru.scisolutions.scicmscore.persistence.entity.Item

class LockFieldBuilder : FieldDefinitionBuilder {
    override fun fromItem(item: Item): FieldDefinition {
        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("lock${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }
}