package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.engine.model.AggregateType

class DatasetFieldInput(
    val name: String,
    val asAlias: String?,
    val aggregate: AggregateType?
)