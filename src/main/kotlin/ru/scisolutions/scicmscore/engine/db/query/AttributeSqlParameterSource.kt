package ru.scisolutions.scicmscore.engine.db.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.util.Json
import java.sql.Types
import java.time.OffsetDateTime
import java.time.OffsetTime

class AttributeSqlParameterSource : MapSqlParameterSource {
    constructor() : super()

    constructor(paramName: String, value: Any?) : super(paramName, value)

    constructor(values: Map<String?, *>?) : super(values)

    fun addValue(paramName: String, value: Any?, type: FieldType): MapSqlParameterSource {
        when (type) {
            FieldType.uuid,
            FieldType.string,
            FieldType.enum,
            FieldType.sequence,
            FieldType.email,
            FieldType.password,
            FieldType.media,
            FieldType.relation -> this.addValue(paramName, value, Types.VARCHAR)
            FieldType.bool -> this.addValue(paramName, value, Types.SMALLINT)
            FieldType.int,
            FieldType.long,
            FieldType.float,
            FieldType.double,
            FieldType.decimal -> this.addValue(paramName, value)
            FieldType.date -> this.addValue(paramName, value, Types.DATE)
            FieldType.time -> this.addValue(paramName, if (value is OffsetTime) value.toLocalTime() else value, Types.TIME)
            FieldType.datetime,
            FieldType.timestamp -> this.addValue(paramName, if (value is OffsetDateTime) value.toLocalDateTime() else value, Types.TIMESTAMP)
            FieldType.text -> this.addValue(paramName, value)
            FieldType.array,
            FieldType.json -> this.addValue(paramName, if (value is String) value else Json.objectMapper.writeValueAsString(value))
        }

        return this
    }
}