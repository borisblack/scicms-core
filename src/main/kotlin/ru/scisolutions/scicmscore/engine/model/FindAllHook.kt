package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection

interface FindAllHook {
    fun beforeFindAll(itemName: String, input: FindAllInput)

    fun afterFindAll(itemName: String, response: ResponseCollection)
}