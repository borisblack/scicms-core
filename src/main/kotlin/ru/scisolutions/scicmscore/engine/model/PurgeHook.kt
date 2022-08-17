package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection

interface PurgeHook {
    fun beforePurge(itemName: String, input: DeleteInput)

    fun afterPurge(itemName: String, response: ResponseCollection)
}