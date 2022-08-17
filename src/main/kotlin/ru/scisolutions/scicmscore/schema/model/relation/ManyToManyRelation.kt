package ru.scisolutions.scicmscore.schema.model.relation

import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ManyToManyRelation : Relation {
    val intermediateItem: Item

    fun getIntermediateTableName(): String = intermediateItem.tableName

    fun getIntermediateSourceAttribute(): Attribute = intermediateItem.spec.getAttributeOrThrow(INTERMEDIATE_SOURCE_ATTR_NAME)

    fun getIntermediateSourceColumnName(): String = getIntermediateSourceAttribute().columnName ?: INTERMEDIATE_SOURCE_ATTR_NAME.lowercase()

    fun getIntermediateTargetAttribute(): Attribute = intermediateItem.spec.getAttributeOrThrow(INTERMEDIATE_TARGET_ATTR_NAME)

    fun getIntermediateTargetColumnName(): String = getIntermediateTargetAttribute().columnName ?: INTERMEDIATE_TARGET_ATTR_NAME.lowercase()

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"
    }
}