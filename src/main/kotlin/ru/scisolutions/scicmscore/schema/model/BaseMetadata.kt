package ru.scisolutions.scicmscore.schema.model

import java.util.Objects

open class BaseMetadata(
    open val name: String // name must start with a lowercase character!
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as BaseMetadata

        return name == other.name
    }

    override fun hashCode(): Int {
        return Objects.hash(name)
    }

    override fun toString(): String = "BaseMetadata(name=$name)"
}