package ru.scisolutions.userimpl.item

import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.response.CustomMethodResponse

class PartItemImpl {
    fun echo(input: CustomMethodInput): CustomMethodResponse = CustomMethodResponse(input.data)
}