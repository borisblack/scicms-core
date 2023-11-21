package ru.scisolutions.scicmscore.engine.db.mapper

import org.springframework.jdbc.core.RowMapper
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.Json
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
            val attrName = item.spec.columnNameToAttrNameMap[columnName] ?: continue
            val attribute = item.spec.attributes[attrName] as Attribute
            val value: Any? = when (attribute.type) {
                FieldType.uuid -> parseUUID(rs.getString(i))
                FieldType.string, FieldType.enum, FieldType.sequence, FieldType.email, FieldType.media, FieldType.relation -> rs.getString(i)
                FieldType.text -> parseText(rs.getObject(i))
                FieldType.password -> rs.getString(i)
                FieldType.int -> rs.getInt(i)
                FieldType.long -> rs.getLong(i)
                FieldType.float -> rs.getFloat(i)
                FieldType.double -> rs.getDouble(i)
                FieldType.decimal -> rs.getBigDecimal(i)
                FieldType.date -> rs.getDate(i)?.toLocalDate()
                FieldType.time -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                FieldType.datetime, FieldType.timestamp ->  rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                FieldType.bool -> rs.getBoolean(i)
                FieldType.array -> {
                    val text: String? = parseText(rs.getObject(i))
                    if (text == null) null else Json.objectMapper.readValue(text, List::class.java)
                }
                FieldType.json -> {
                    val text: String? = parseText(rs.getObject(i))
                    if (text == null) null else Json.objectMapper.readValue(text, Map::class.java)
                }
            }

            itemRec[attrName] = value
        }

        return itemRec
    }

    private fun parseUUID(value: String?): UUID? = if (value == null) null else UUID.fromString(value)

    private fun parseText(value: Any?): String? = if (value is Clob) value.characterStream.readText() else value as String?
}