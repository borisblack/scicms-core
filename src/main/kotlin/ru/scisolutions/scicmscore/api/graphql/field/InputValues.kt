package ru.scisolutions.scicmscore.api.graphql.field

import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import ru.scisolutions.scicmscore.api.graphql.TypeNames

object InputValues {
    private const val MAJOR_REV_ATTR_NAME = "majorRev"
    private const val RELEASED_ATTR_NAME = "released"
    private const val LOCALE_ATTR_NAME = "locale"

    val MAJOR_REV: InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(MAJOR_REV_ATTR_NAME)
            .type(TypeNames.STRING)
            .build()

    val NON_NULL_MAJOR_REV: InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(MAJOR_REV_ATTR_NAME)
            .type(NonNullType(TypeNames.STRING))
            .build()

    val RELEASED: InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(RELEASED_ATTR_NAME)
            .type(TypeNames.BOOLEAN)
            .build()

    val LOCALE: InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(LOCALE_ATTR_NAME)
            .type(TypeNames.STRING)
            .build()

    val NON_NULL_LOCALE: InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(LOCALE_ATTR_NAME)
            .type(NonNullType(TypeNames.STRING))
            .build()
}