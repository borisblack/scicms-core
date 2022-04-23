package ru.scisolutions.scicmscore.api.graphql.field.builder

import graphql.language.FieldDefinition

interface FieldDefinitionListBuilder {
    fun buildList(): List<FieldDefinition>
}