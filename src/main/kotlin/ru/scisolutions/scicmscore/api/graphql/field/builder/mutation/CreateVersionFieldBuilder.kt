package ru.scisolutions.scicmscore.api.graphql.field.builder.mutation

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.InputValues
import ru.scisolutions.scicmscore.persistence.entity.Item

class CreateVersionFieldBuilder : FieldDefinitionBuilder {
    override fun fromItem(item: Item): FieldDefinition {
        if (!item.versioned)
            throw IllegalStateException("Item [${item.name}] is not versioned. CreateVersion mutation cannot be applied")

        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}Version")
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

        if (item.manualVersioning)
            builder.inputValueDefinition(InputValues.NON_NULL_MAJOR_REV)

        if (item.localized)
            builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }
}