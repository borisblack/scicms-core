package ru.scisolutions.scicmscore.engine.data.handler.util

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class RelationHelper(
    private val relationManager: RelationManager,
    private val itemRecDao: ItemRecDao
) {
    fun updateRelations(item: Item, itemRecId: String, relAttributes: Map<String, Any>) {
        relAttributes.forEach { (attrName, value) ->
            updateRelation(item, itemRecId, attrName, value)
        }
    }

    fun updateRelation(item: Item, itemRecId: String, relAttrName: String, relAttrValue: Any) {
        val attribute = item.spec.getAttributeOrThrow(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                if (relation.isOwning) {
                    val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                    itemRecDao.updateById(relation.inversedItem, relAttrValue as String, inversedItemRec)
                } else {
                    val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                    itemRecDao.updateById(relation.owningItem, relAttrValue as String, owningItemRec)
                }
            }
            is OneToManyInversedBidirectionalRelation -> {
                val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                itemRecDao.updateById(relation.inversedItem, relAttrValue as String, inversedItemRec)
            }
            is ManyToManyRelation -> {
                relAttrValue as Collection<*>
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as String) }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning)
                            relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as String) }
                        else
                            relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem, it as String, itemRecId) }
                    }
                }
            }
        }
    }

    private fun addManyToManyRelation(intermediateItem: Item, sourceId: String, targetId: String) {
        val intermediateItemRec = ItemRec(
            mutableMapOf(
                INTERMEDIATE_SOURCE_ATTR_NAME to sourceId,
                INTERMEDIATE_TARGET_ATTR_NAME to targetId
            )
        )
        itemRecDao.insertWithDefaults(intermediateItem, intermediateItemRec)
    }

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"
    }
}