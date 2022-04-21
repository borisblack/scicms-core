package ru.scisolutions.scicmscore.graphql.inputvalue.builder

import graphql.language.InputValueDefinition
import ru.scisolutions.scicmscore.graphql.TypeNames

class LocaleInputValueBuilder {
    fun build(): InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(LOCALE_ATTR_NAME)
            .type(TypeNames.STRING)
            .build()

    companion object {
        private const val LOCALE_ATTR_NAME = "locale"
    }
}