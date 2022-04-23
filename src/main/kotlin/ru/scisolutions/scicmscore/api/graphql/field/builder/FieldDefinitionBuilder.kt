package ru.scisolutions.scicmscore.api.graphql.field.builder

import graphql.language.FieldDefinition

interface FieldDefinitionBuilder {
    fun build(): FieldDefinition
}