package ru.scisolutions.scicmscore.api.graphql.field.builder.mutation

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionBuilder

class PurgeFieldBuilder(private val item: Item) : FieldDefinitionBuilder {
    override fun build(): FieldDefinition {
        if (!item.versioned)
            throw IllegalStateException("Item [${item.name}] is not versioned. Purge mutation cannot be applied")

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
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }
}