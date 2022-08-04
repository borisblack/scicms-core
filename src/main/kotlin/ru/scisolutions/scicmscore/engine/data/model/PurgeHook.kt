package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

interface PurgeHook {
    fun beforePurge(itemName: String, input: DeleteInput)

    fun afterPurge(itemName: String, response: ResponseCollection)
}