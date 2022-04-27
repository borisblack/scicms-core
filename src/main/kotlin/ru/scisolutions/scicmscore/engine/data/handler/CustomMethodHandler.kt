package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse

interface CustomMethodHandler {
    fun getCustomMethods(itemName: String): Set<String>

    fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse
}