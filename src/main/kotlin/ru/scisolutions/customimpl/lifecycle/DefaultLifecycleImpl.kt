package ru.scisolutions.customimpl.lifecycle

import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.engine.schema.model.Promotable

class DefaultLifecycleImpl : Promotable {
    override fun promote(itemName: String, id: String, state: String) {
        logger.info("Promote called. Item name = $itemName, ID = $id, state = $state")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultLifecycleImpl::class.java)
    }
}