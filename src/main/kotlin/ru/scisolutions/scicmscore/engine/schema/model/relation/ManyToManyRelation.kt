package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

interface ManyToManyRelation : Relation {
    val intermediateItem: Item

    fun getIntermediateTableName(): String = requireNotNull(intermediateItem.tableName)

    fun getIntermediateSourceAttribute(): Attribute = intermediateItem.spec.getAttribute(INTERMEDIATE_SOURCE_ATTR_NAME)

    fun getIntermediateSourceColumnName(): String = getIntermediateSourceAttribute().columnName ?: INTERMEDIATE_SOURCE_ATTR_NAME.lowercase()

    fun getIntermediateTargetAttribute(): Attribute = intermediateItem.spec.getAttribute(INTERMEDIATE_TARGET_ATTR_NAME)

    fun getIntermediateTargetColumnName(): String = getIntermediateTargetAttribute().columnName ?: INTERMEDIATE_TARGET_ATTR_NAME.lowercase()

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"
    }
}
