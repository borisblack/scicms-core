package ru.scisolutions.scicmscore.graphql.inputvalue.builder

import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import ru.scisolutions.scicmscore.graphql.TypeNames

class MajorRevInputValueBuilder(private val required: Boolean = false) {
    fun build(): InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(MAJOR_REV_ATTR_NAME)
            .type(if (required) NonNullType(TypeNames.STRING) else TypeNames.STRING)
            .build()

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
    }
}