package ru.scisolutions.scicmscore.graphql.inputvalue.builder

import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName

class MajorRevInputValueBuilder(private val required: Boolean = false) {
    fun build(): InputValueDefinition {
        val stringType = TypeName("String")
        return InputValueDefinition.newInputValueDefinition()
            .name(MAJOR_REV_ATTR_NAME)
            .type(if (required) NonNullType(stringType) else stringType)
            .build()
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
    }
}