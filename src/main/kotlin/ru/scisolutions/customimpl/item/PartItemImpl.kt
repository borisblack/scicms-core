package ru.scisolutions.customimpl.item

import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.response.CustomMethodResponse

class PartItemImpl {
    fun echo(input: CustomMethodInput): CustomMethodResponse = CustomMethodResponse(input.data)
}