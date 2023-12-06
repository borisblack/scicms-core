package ru.scisolutions.scicmscore.engine.hook.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response

@Service
class MediaItemImpl(private val mediaHandler: MediaHandler) : DeleteHook {
    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        mediaHandler.deleteById(input.id)
    }

    override fun afterDelete(itemName: String, response: Response) {
        // Do nothing
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaItemImpl::class.java)
    }
}