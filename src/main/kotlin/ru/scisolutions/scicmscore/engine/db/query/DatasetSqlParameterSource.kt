package ru.scisolutions.scicmscore.engine.db.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import ru.scisolutions.scicmscore.persistence.entity.Dataset.TemporalType
import java.sql.Types
import java.time.OffsetDateTime
import java.time.OffsetTime

class DatasetSqlParameterSource : MapSqlParameterSource {
    constructor() : super()

    constructor(paramName: String, value: Any?) : super(paramName, value)

    constructor(values: Map<String?, *>?) : super(values)

    fun addValue(paramName: String, value: Any?, type: TemporalType): MapSqlParameterSource {
        when (type) {
            TemporalType.date -> this.addValue(paramName, value, Types.DATE)
            TemporalType.time -> this.addValue(paramName, if (value is OffsetTime) value.toLocalTime() else value, Types.TIME)
            TemporalType.datetime,
            TemporalType.timestamp -> this.addValue(paramName, if (value is OffsetDateTime) value.toLocalDateTime() else value, Types.TIMESTAMP)
        }

        return this
    }
}