package ru.scisolutions.userimpl.item

import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse

class PartItemImplementation {
    fun echo(input: CustomMethodInput): CustomMethodResponse = CustomMethodResponse(input.data)
}