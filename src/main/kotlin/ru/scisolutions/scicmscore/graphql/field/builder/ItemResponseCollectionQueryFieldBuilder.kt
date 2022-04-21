package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.LocaleInputValueBuilder
import ru.scisolutions.scicmscore.graphql.inputvalue.builder.MajorRevInputValueBuilder

class ItemResponseCollectionQueryFieldBuilder(private val item: Item) {
    fun build(): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.pluralName)
            .type(TypeName("${name}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("filters")
                    .type(TypeName("${name}FiltersInput"))
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
                    .type(ListType(TypeName("String")))
                    .build()
            )

        if (item.versioned)
            builder.inputValueDefinition(MajorRevInputValueBuilder().build())

        if (item.localized)
            builder.inputValueDefinition(LocaleInputValueBuilder().build())

        return builder.build()
    }
}