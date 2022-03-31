package ru.scisolutions.scicmscore.dbschema.model

class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)