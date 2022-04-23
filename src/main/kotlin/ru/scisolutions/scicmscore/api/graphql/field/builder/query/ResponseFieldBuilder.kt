package ru.scisolutions.scicmscore.api.graphql.field.builder.query

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.api.graphql.field.builder.FieldDefinitionBuilder

class ResponseFieldBuilder(private val item: Item) : FieldDefinitionBuilder {
    override fun build(): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.capitalize()}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )

        // if (item.versioned)
        //     builder.inputValueDefinition(InputValues.MAJOR_REV)
        //
        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }
}