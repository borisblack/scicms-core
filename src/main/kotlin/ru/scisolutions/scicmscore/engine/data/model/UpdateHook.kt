package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface UpdateHook {
    fun beforeUpdate(itemName: String, input: UpdateInput)

    fun afterUpdate(itemName: String, response: Response)
}