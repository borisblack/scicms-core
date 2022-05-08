package ru.scisolutions.scicmscore.api.graphql.type.builder.input

import graphql.language.InputObjectTypeDefinition
import ru.scisolutions.scicmscore.persistence.entity.Item

interface InputObjectTypeBuilder {
    fun fromItem(item: Item): InputObjectTypeDefinition
}