package ru.scisolutions.scicmscore.engine.persistence.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.util.Json
import java.sql.Types
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset

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
            FieldType.relation,
            -> this.addValue(paramName, value, Types.VARCHAR)
            FieldType.bool -> this.addValue(paramName, value, Types.SMALLINT)
            FieldType.int,
            FieldType.long,
            FieldType.float,
            FieldType.double,
            FieldType.decimal,
            -> this.addValue(paramName, value)
            FieldType.date -> this.addValue(paramName, value, Types.DATE)
            FieldType.time ->
                this.addValue(
                    paramName,
                    if (value is OffsetTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value,
                    Types.TIME_WITH_TIMEZONE,
                )
            FieldType.datetime,
            FieldType.timestamp,
            ->
                this.addValue(
                    paramName,
                    if (value is OffsetDateTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value,
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )
            FieldType.text -> this.addValue(paramName, value)
            FieldType.array,
            FieldType.json,
            -> this.addValue(paramName, if (value is String) value else Json.objectMapper.writeValueAsString(value))
        }

        return this
    }
}
