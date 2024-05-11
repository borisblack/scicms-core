package ru.scisolutions.scicmscore.api.graphql.field

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.extension.upperFirst

@Component
class QueryItemFields {
    fun item(item: Item): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.upperFirst()}Response"))
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

    fun itemCollection(item: Item): FieldDefinition {
        val capitalizedItemName = item.name.upperFirst()
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

        builder.inputValueDefinition(InputValues.STATE)

        return builder.build()
    }
}