package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface LockHandler {
    fun lock(itemName: String, id: String, selectAttrNames: Set<String>): Response

    fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): Response
}