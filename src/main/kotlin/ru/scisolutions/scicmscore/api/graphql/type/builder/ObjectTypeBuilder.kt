package ru.scisolutions.scicmscore.api.graphql.type.builder

import graphql.language.ObjectTypeDefinition
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ObjectTypeBuilder {
    fun fromItem(item: Item): ObjectTypeDefinition
}