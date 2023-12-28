package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.model.Column

class Table(
    val name: String,
    val columns: Map<String, Column>
)