package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.response.Response

interface FindOneHook {
    fun beforeFindOne(itemName: String, id: String)

    fun afterFindOne(itemName: String, response: Response)
}
