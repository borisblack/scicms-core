package ru.scisolutions.scicmscore.engine.model.input

import java.util.UUID

class CreateLocalizationInput(
    val id: UUID,
    val data: Map<String, Any?>,
    val locale: String,
    val copyCollectionRelations: Boolean? = null
)