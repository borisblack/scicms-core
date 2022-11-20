package ru.scisolutions.scicmscore.schema.model

import java.util.Objects

open class BaseMetadata(
    // name and pluralName must start with a lowercase character!
    open val name: String,
    open val pluralName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as BaseMetadata

        return name == other.name && pluralName == other.pluralName
    }

    override fun hashCode(): Int = Objects.hash(name, pluralName)

    override fun toString(): String = "BaseMetadata(name=$name, pluralName=$pluralName)"
}