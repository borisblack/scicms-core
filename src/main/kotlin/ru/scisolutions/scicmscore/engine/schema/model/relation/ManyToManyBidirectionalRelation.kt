package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToManyBidirectionalRelation(
    val isOwning: Boolean,

    owningItem: Item,
    owningAttrName: String,

    inversedItem: Item,
    inversedAttrName: String,

    val intermediateItem: Item,
    val sourceIntermediateAttrName: String = ManyToManyRelation.SOURCE_ATTR_NAME,
    val targetIntermediateAttrName: String = ManyToManyRelation.TARGET_ATTR_NAME
) : BidirectionalRelation(owningItem, owningAttrName, inversedItem, inversedAttrName), ManyToManyRelation {
    val intermediateTableName: String = intermediateItem.tableName
    val sourceIntermediateAttribute: Attribute = intermediateItem.spec.getAttributeOrThrow(sourceIntermediateAttrName)
    val targetIntermediateAttribute: Attribute = intermediateItem.spec.getAttributeOrThrow(targetIntermediateAttrName)
    val sourceIntermediateColumnName: String = sourceIntermediateAttribute.columnName ?: sourceIntermediateAttrName.lowercase()
    val targetIntermediateColumnName: String = targetIntermediateAttribute.columnName ?: targetIntermediateAttrName.lowercase()

    val owningKeyAttrName: String = sourceIntermediateAttribute.extractTargetKeyAttrName()
    val owningKeyAttribute: Attribute = owningItem.spec.getAttributeOrThrow(owningKeyAttrName)
    val owningKeyColumnName: String = owningKeyAttribute.columnName ?: owningKeyAttrName.lowercase()

    val inversedKeyAttrName: String = targetIntermediateAttribute.extractTargetKeyAttrName()
    val inversedKeyAttribute: Attribute = inversedItem.spec.getAttributeOrThrow(inversedKeyAttrName)
    val inversedKeyColumnName: String = inversedKeyAttribute.columnName ?: inversedKeyAttrName.lowercase()
}