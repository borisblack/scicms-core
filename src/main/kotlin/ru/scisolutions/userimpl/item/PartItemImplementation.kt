package ru.scisolutions.userimpl.item

import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse

class PartItemImplementation {
    fun send(input: CustomMethodInput): CustomMethodResponse {
        return CustomMethodResponse(
            data = mapOf(
                "message" to "Success",
                "greeting" to input.data?.get("greeting")
            )
        )
    }
}