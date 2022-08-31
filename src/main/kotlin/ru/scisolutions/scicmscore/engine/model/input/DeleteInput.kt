package ru.scisolutions.scicmscore.engine.model.input

import java.util.UUID

class DeleteInput(
    val id: UUID,
    val deletingStrategy: DeletingStrategy
) {
    enum class DeletingStrategy {
        NO_ACTION,
        SET_NULL,
        CASCADE
    }
}