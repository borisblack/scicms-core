package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.engine.model.response.Response
import java.util.UUID

interface FindOneHook {
    fun beforeFindOne(itemName: String, id: UUID)

    fun afterFindOne(itemName: String, response: Response)
}