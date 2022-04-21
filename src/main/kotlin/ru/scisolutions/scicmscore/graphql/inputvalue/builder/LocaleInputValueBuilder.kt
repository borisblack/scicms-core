package ru.scisolutions.scicmscore.graphql.inputvalue.builder

import graphql.language.InputValueDefinition
import graphql.language.TypeName

class LocaleInputValueBuilder {
    fun build(): InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(LOCALE_ATTR_NAME)
            .type(TypeName("String"))
            .build()

    companion object {
        private const val LOCALE_ATTR_NAME = "locale"
    }
}