package ru.scisolutions.scicmscore.engine.model.input

import java.util.UUID

class UpdateInput(
    val id: UUID,
    val data: Map<String, Any?>
)