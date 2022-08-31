package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.util.UUID
import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

@Component
class DeleteMediaHelper(
    private val mediaHandler: MediaHandler
) {
    fun processMedia(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> attribute.type == AttrType.media && itemRec[attrName] != null }
            .forEach { (attrName, _) ->
                val mediaId = itemRec[attrName] as UUID
                deleteMediaById(mediaId)
            }
    }

    private fun deleteMediaById(id: UUID) {
        try {
            mediaHandler.deleteById(id)
        } catch (e: Exception) {
            logger.warn("Cannot delete media. {}", e.message)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteMediaHelper::class.java)
    }
}