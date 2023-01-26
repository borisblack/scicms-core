package ru.scisolutions.scicmscore.engine.model.input

class PaginationInput(
    val page: Int? = null,
    val pageSize: Int? = null,
    val start: Int? = null,
    val limit: Int? = null
) {
    companion object {
        fun fromMap(map: Map<String, Int>) =
            PaginationInput(
                page = map["page"],
                pageSize = map["pageSize"],
                start = map["start"],
                limit = map["limit"]
            )
    }
}