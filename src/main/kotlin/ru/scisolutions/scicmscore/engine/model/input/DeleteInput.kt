package ru.scisolutions.scicmscore.engine.model.input

class DeleteInput(
    val id: String,
    val deletingStrategy: DeletingStrategy
) {
    enum class DeletingStrategy {
        NO_ACTION,
        SET_NULL,
        CASCADE
    }
}