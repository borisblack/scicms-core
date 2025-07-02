package ru.scisolutions.scicmscore.engine.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import java.util.Objects

@JsonInclude(Include.NON_NULL)
data class Column(
    val type: FieldType? = null,
    val custom: Boolean = false,
    val hidden: Boolean = false,
    val source: String? = null,
    val aggregate: AggregateType? = null,
    val formula: String? = null,
    val alias: String? = null,
    val format: String? = null,
    val colWidth: Int? = null,

    /**
     * Row level security entries.
     */
    val rls: List<RlsEntry>? = null
) {
    class RlsEntry(
        /** Checked value */
        val value: String,

        /** Allowed identities (users and roles). Roles must begin with the prefix '@role:' */
        val identities: Set<String>,

        /** Flag indicating if any identity is allowed by rule */
        val anyIdentity: Boolean,

        /** Flag indicating if rule is active */
        val active: Boolean,
    ) {
        @get:JsonIgnore
        val anyValue: Boolean
            get() = value == "*"

        @get:JsonIgnore
        val users: Set<String>
            get() = identities.filter { !it.startsWith(ROLE_PREFIX) }.toSet()

        @get:JsonIgnore
        val roles: Set<String>
            get() = identities.filter { it.startsWith(ROLE_PREFIX) }.map { it.removePrefix(ROLE_PREFIX) }.toSet()

        fun validate(colName: String) {
            // Check if any value is allowed for any identity
            if (anyValue && anyIdentity)
                throw IllegalStateException("Invalid RLS definition for column [$colName]. Any value cannot be allowed for any identity.")

            // Check that if all identities are allowed, then the user and role lists are empty.
            if (anyIdentity && identities.isNotEmpty())
                throw IllegalStateException("Invalid RLS identities definition for column [$colName]. All identities are allowed, but the personal identities list is not empty.")
        }

        companion object {
            private const val ROLE_PREFIX = "@role:"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as Column
        return type != other.type && custom == other.custom && source == other.source && formula == other.formula &&
            hidden == other.hidden && aggregate == other.aggregate && alias == other.alias && format == other.format &&
            colWidth == other.colWidth
    }

    override fun hashCode(): Int = Objects.hash(
        type, custom, source, formula, hidden, aggregate?.name, alias, colWidth
    )

    @get:JsonIgnore
    val typeRequired: FieldType
        get() = type ?: throw IllegalStateException("Type must be not null")

    fun validate(colName: String) {
        rls?.forEach { it.validate(colName) }
    }
}
