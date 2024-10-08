package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

@Component
class DeleteMediaHelper(
    private val mediaHandler: MediaHandler
) {
    fun deleteMedia(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> attribute.type == FieldType.media && itemRec[attrName] != null }
            .forEach { (attrName, _) ->
                val mediaId = itemRec[attrName] as String
                deleteMediaById(mediaId)
            }
    }

    private fun deleteMediaById(id: String) {
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
