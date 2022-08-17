package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.response.Response

interface LockHandler {
    fun lock(itemName: String, id: String, selectAttrNames: Set<String>): Response

    fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): Response
}