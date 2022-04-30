package ru.scisolutions.scicmscore.engine.data.db.impl

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.Base64Encoding
import java.sql.Clob
import java.sql.ResultSet
import java.time.ZoneOffset

class ItemRecMapper(private val item: Item) : RowMapper<ItemRec> {
    override fun map(rs: ResultSet, ctx: StatementContext): ItemRec {
        val itemRec = ItemRec()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val columnName = metaData.getColumnName(i).lowercase()
            val attrName = item.spec.columnNameToAttrNameMap[columnName] as String
            val attribute = item.spec.attributes[attrName] as Attribute
            val value = when (attribute.type) {
                Type.uuid, Type.string, Type.enum, Type.sequence, Type.email, Type.media, Type.relation -> rs.getString(i)
                Type.text, Type.array, Type.json -> {
                    val obj = rs.getObject(i)
                    if (obj is Clob) obj.characterStream.readText() else obj
                }
                Type.password -> Base64Encoding.decodeNullable(rs.getString(i))
                Type.int -> rs.getInt(i)
                Type.long -> rs.getLong(i)
                Type.float -> rs.getFloat(i)
                Type.double -> rs.getDouble(i)
                Type.decimal -> rs.getBigDecimal(i)
                Type.date -> rs.getDate(i)?.toLocalDate()
                Type.time -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                Type.datetime, Type.timestamp ->  rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                Type.bool -> rs.getBoolean(i)
                else -> rs.getObject(i)
            }

            itemRec[attrName] = value
        }

        return itemRec
    }
}