package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateHook {
    fun beforeCreate(itemName: String, input: CreateInput)

    fun afterCreate(itemName: String, response: Response)
}