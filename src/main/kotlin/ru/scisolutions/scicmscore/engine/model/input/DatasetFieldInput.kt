package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.engine.model.AggregateType

class DatasetFieldInput(
    val name: String,
    val source: String?,
    val aggregate: AggregateType?
)