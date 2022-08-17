package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.response.CustomMethodResponse

interface CustomMethodHandler {
    fun getCustomMethods(itemName: String): Set<String>

    fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse
}