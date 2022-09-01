package ru.scisolutions.scicmscore.engine.db

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.jdbc.core.RowMapper
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.sql.Clob
import java.sql.ResultSet
import java.time.ZoneOffset
import java.util.UUID

class ItemRecMapper(private val item: Item) : RowMapper<ItemRec> {
    override fun mapRow(rs: ResultSet, rowNum: Int): ItemRec {
        val itemRec = ItemRec()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val columnName = metaData.getColumnName(i).lowercase()
            val attrName = item.spec.columnNameToAttrNameMap[columnName] as String
            val attribute = item.spec.attributes[attrName] as Attribute
            val value: Any? = when (attribute.type) {
                Type.uuid -> parseUUID(rs.getString(i))
                Type.string, Type.enum, Type.sequence, Type.email, Type.media, Type.location, Type.relation -> rs.getString(i)
                Type.text -> parseText(rs.getObject(i))
                Type.password -> rs.getString(i)
                Type.int -> rs.getInt(i)
                Type.long -> rs.getLong(i)
                Type.float -> rs.getFloat(i)
                Type.double -> rs.getDouble(i)
                Type.decimal -> rs.getBigDecimal(i)
                Type.date -> rs.getDate(i)?.toLocalDate()
                Type.time -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                Type.datetime, Type.timestamp ->  rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                Type.bool -> rs.getBoolean(i)
                Type.array -> {
                    val text: String? = parseText(rs.getObject(i))
                    if (text == null) null else objectMapper.readValue(text, List::class.java)
                }
                Type.json -> {
                    val text: String? = parseText(rs.getObject(i))
                    if (text == null) null else objectMapper.readValue(text, Map::class.java)
                }
            }

            itemRec[attrName] = value
        }

        return itemRec
    }

    private fun parseUUID(value: String?): UUID? = if (value == null) null else UUID.fromString(value)

    private fun parseText(value: Any?): String? = if (value is Clob) value.characterStream.readText() else value as String?

    companion object {
        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}