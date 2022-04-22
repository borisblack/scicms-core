package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition

interface FieldDefinitionListBuilder {
    fun buildList(): List<FieldDefinition>
}