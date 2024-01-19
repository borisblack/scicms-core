package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.engine.model.AggregateType
import ru.scisolutions.scicmscore.model.FieldType

class DatasetFieldInput(
    val name: String,
    val type: FieldType,
    val custom: Boolean,
    val source: String?,
    val aggregate: AggregateType?,
    val formula: String?
)