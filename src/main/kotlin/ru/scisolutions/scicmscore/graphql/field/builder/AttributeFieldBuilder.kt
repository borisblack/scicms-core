package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.graphql.TypeResolver

class AttributeFieldBuilder(private val attrName: String, private val attribute: Attribute) {
    fun build(): FieldDefinition =
        FieldDefinition.newFieldDefinition()
            .name(attrName)
            .type(typeResolver.objectType(attrName, attribute))
            .build()

    companion object {
        private val typeResolver = TypeResolver()
    }
}