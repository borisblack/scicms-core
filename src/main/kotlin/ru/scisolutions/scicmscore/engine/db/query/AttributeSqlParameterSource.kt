package ru.scisolutions.scicmscore.engine.db.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.sql.Types
import java.time.OffsetDateTime
import java.time.OffsetTime
import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

class AttributeSqlParameterSource : MapSqlParameterSource {
    constructor() : super()

    constructor(paramName: String, value: Any?) : super(paramName, value)

    constructor(values: Map<String?, *>?) : super(values)

    fun addValue(paramName: String, value: Any?, type: AttrType): MapSqlParameterSource {
        when (type) {
            AttrType.uuid,
            AttrType.string,
            AttrType.enum,
            AttrType.sequence,
            AttrType.email,
            AttrType.password,
            AttrType.media,
            AttrType.location,
            AttrType.relation -> this.addValue(paramName, value, Types.VARCHAR)
            AttrType.bool -> this.addValue(paramName, value, Types.SMALLINT)
            AttrType.int,
            AttrType.long,
            AttrType.float,
            AttrType.double,
            AttrType.decimal -> this.addValue(paramName, value)
            AttrType.date -> this.addValue(paramName, value, Types.DATE)
            AttrType.time -> this.addValue(paramName, if (value is OffsetTime) value.toLocalTime() else value, Types.TIME)
            AttrType.datetime,
            AttrType.timestamp -> this.addValue(paramName, if (value is OffsetDateTime) value.toLocalDateTime() else value, Types.TIMESTAMP)
            AttrType.text,
            AttrType.array,
            AttrType.json -> this.addValue(paramName, value)
        }

        return this
    }
}