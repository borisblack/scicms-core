package ru.scisolutions.scicmscore.engine.data.db.impl

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.schema.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Attribute.Type
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
            val attrName = item.spec.columns[columnName] as String
            val attribute = item.spec.attributes[attrName] as Attribute
            val value = when (attribute.type) {
                Type.UUID.value, Type.STRING.value, Type.ENUM.value, Type.SEQUENCE.value, Type.EMAIL.value -> rs.getString(i)
                Type.TEXT.value, Type.ARRAY.value, Type.JSON.value -> {
                    val obj = rs.getObject(i)
                    if (obj is Clob) obj.characterStream.readText() else obj
                }
                Type.PASSWORD.value -> Base64Encoding.decodeNullable(rs.getString(i))
                Type.DATE.value -> rs.getDate(i)?.toLocalDate()
                Type.TIME.value -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                Type.DATETIME.value, Type.TIMESTAMP.value ->  rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                Type.BOOL.value -> rs.getBoolean(i)
                else -> rs.getObject(i)
            }

            itemRec[attrName] = value
        }

        return itemRec
    }
}