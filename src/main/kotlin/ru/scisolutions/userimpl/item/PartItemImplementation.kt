package ru.scisolutions.userimpl.item

import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.response.CustomMethodResponse

class PartItemImplementation {
    fun echo(input: CustomMethodInput): CustomMethodResponse = CustomMethodResponse(input.data)
}