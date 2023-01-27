package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetHandler {
    fun load(dataset: Dataset, input: DatasetInput): DatasetResponse
}