package ru.scisolutions.scicmscore.engine.data.db.impl

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.Base64Encoding
import java.sql.Clob
import java.sql.ResultSet
import java.time.ZoneOffset
import ru.scisolutions.scicmscore.domain.model.Attribute.Type as AttrType

class ItemRecMapper(private val item: Item) : RowMapper<ItemRec> {
    override fun map(rs: ResultSet, ctx: StatementContext): ItemRec {
        val itemRec = ItemRec()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val columnName = metaData.getColumnName(i).lowercase()
            val attrName = item.spec.columnNameToAttrNameMap[columnName] as String
            val attribute = item.spec.attributes[attrName] as Attribute
            val value = when (AttrType.valueOf(attribute.type)) {
                AttrType.uuid, AttrType.string, AttrType.enum, AttrType.sequence, AttrType.email, AttrType.media, AttrType.relation -> rs.getString(i)
                AttrType.text, AttrType.array, AttrType.json -> {
                    val obj = rs.getObject(i)
                    if (obj is Clob) obj.characterStream.readText() else obj
                }
                AttrType.password -> Base64Encoding.decodeNullable(rs.getString(i))
                AttrType.date -> rs.getDate(i)?.toLocalDate()
                AttrType.time -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                AttrType.datetime, AttrType.timestamp ->  rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                AttrType.bool -> rs.getBoolean(i)
                else -> rs.getObject(i)
            }

            itemRec[attrName] = value
        }

        return itemRec
    }
}