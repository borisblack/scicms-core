package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse

interface DatasetHandler {
    fun load(datasetName: String, input: DatasetInput): DatasetResponse
}