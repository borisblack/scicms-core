package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface DeleteHook {
    fun beforeDelete(itemName: String, input: DeleteInput)

    fun afterDelete(itemName: String, response: Response)
}