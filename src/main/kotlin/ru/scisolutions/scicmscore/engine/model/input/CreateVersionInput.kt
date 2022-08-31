package ru.scisolutions.scicmscore.engine.model.input

import java.util.UUID

class CreateVersionInput(
    val id: UUID,
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null,
    val copyCollectionRelations: Boolean? = null
)