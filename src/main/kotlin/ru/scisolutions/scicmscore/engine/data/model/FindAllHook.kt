package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

interface FindAllHook {
    fun beforeFindAll(itemName: String, input: FindAllInput)

    fun afterFindAll(itemName: String, response: ResponseCollection)
}