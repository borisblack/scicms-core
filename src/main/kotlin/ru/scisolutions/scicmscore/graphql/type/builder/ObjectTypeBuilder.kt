package ru.scisolutions.scicmscore.graphql.type.builder

import graphql.language.ObjectTypeDefinition

interface ObjectTypeBuilder {
    fun build(): ObjectTypeDefinition
}