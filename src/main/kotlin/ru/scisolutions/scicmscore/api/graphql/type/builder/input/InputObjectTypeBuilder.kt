package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition

interface InputObjectTypeBuilder {
    fun build(): InputObjectTypeDefinition
}