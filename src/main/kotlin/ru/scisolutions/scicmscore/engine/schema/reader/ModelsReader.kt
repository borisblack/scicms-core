package ru.scisolutions.scicmscore.engine.schema.reader

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel

interface ModelsReader {
    fun read(): Collection<AbstractModel>
}