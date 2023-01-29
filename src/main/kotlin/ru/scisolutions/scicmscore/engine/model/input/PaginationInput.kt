package ru.scisolutions.scicmscore.engine.model.input

class PaginationInput(
    val page: Int? = null,
    val pageSize: Int? = null,
    val start: Int? = null,
    val limit: Int? = null
) {
    companion object {
        fun fromMap(map: Map<String, Any>): PaginationInput {
            val page = map["page"]
            val pageSize = map["pageSize"]
            val start = map["start"]
            val limit = map["limit"]

            return PaginationInput(
                page = if (page is String) page.toInt() else page as Int?,
                pageSize = if (pageSize is String) pageSize.toInt() else pageSize as Int?,
                start = if (start is String) start.toInt() else start as Int?,
                limit = if (limit is String) limit.toInt() else limit as Int?
            )
        }
    }
}