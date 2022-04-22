package ru.scisolutions.scicmscore.graphql.field.builder

import graphql.language.FieldDefinition

interface FieldDefinitionBuilder {
    fun build(): FieldDefinition
}