package ru.scisolutions.scicmscore.api.graphql.field.builder

import graphql.language.FieldDefinition
import ru.scisolutions.scicmscore.persistence.entity.Item

interface FieldDefinitionBuilder {
    fun fromItem(item: Item): FieldDefinition
}