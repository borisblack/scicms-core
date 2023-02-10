package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface DeleteHook {
    fun beforeDelete(itemName: String, input: DeleteInput)

    fun afterDelete(itemName: String, response: Response)
}