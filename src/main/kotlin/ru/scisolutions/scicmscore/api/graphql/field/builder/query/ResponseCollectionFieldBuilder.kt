package ru.scisolutions.scicmscore.api.graphql.field.builder.query

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.InputValues

class ResponseCollectionFieldBuilder(private val item: Item) : FieldDefinitionBuilder {
    override fun build(): FieldDefinition {
        val capitalizedItemName = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.pluralName)
            .type(TypeName("${capitalizedItemName}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("filters")
                    .type(TypeName("${capitalizedItemName}FiltersInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("pagination")
                    .type(TypeName("PaginationInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("sort")
                    .type(ListType(TypeNames.STRING))
                    .build()
            )

        if (item.versioned)
            builder.inputValueDefinition(InputValues.MAJOR_REV)

        if (item.localized)
            builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }
}