package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface FindOneHook {
    fun beforeFindOne(itemName: String, id: String)

    fun afterFindOne(itemName: String, response: Response)
}