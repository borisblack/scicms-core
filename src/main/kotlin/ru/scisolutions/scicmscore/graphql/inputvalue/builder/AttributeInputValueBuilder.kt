package ru.scisolutions.scicmscore.graphql.inputvalue.builder

import graphql.language.InputValueDefinition
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.graphql.TypeResolver

class AttributeInputValueBuilder(private val attrName: String, private val attribute: Attribute) {
    fun build(): InputValueDefinition = InputValueDefinition.newInputValueDefinition()
        .name(attrName)
        .type(typeResolver.inputType(attrName, attribute))
        .build()

    companion object {
        private val typeResolver = TypeResolver()
    }
}