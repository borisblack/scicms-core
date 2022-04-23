package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.ObjectTypeDefinition

interface ObjectTypeBuilder {
    fun build(): ObjectTypeDefinition
}