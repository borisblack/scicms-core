package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateVersionHook {
    fun beforeCreateVersion(itemName: String, input: CreateVersionInput)

    fun afterCreateVersion(itemName: String, response: Response)
}