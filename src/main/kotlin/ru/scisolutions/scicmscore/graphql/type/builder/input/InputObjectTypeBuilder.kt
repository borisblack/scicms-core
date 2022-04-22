package ru.scisolutions.scicmscore.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition

interface InputObjectTypeBuilder {
    fun build(): InputObjectTypeDefinition
}