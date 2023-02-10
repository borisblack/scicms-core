package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface UpdateHook {
    fun beforeUpdate(itemName: String, input: UpdateInput)

    fun afterUpdate(itemName: String, response: Response)
}