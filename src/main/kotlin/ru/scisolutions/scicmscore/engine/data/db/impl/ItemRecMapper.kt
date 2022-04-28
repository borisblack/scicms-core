package ru.scisolutions.scicmscore.engine.data.db.impl

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import ru.scisolutions.scicmscore.util.Base64Encoding
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.schema.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.sql.Clob
import java.sql.ResultSet

class ItemRecMapper(private val item: Item) : RowMapper<ItemRec> {
    override fun map(rs: ResultSet, ctx: StatementContext): ItemRec {
        val itemRec = ItemRec()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val columnName = metaData.getColumnName(i).lowercase()
            val attrName = item.spec.columns[columnName] as String
            val attribute = item.spec.attributes[attrName] as Attribute
            val value = when (attribute.type) {
                Type.PASSWORD.value -> Base64Encoding.decodeOrNull(rs.getString(i))
                Type.BOOL.value -> rs.getBoolean(i)
                else -> parseObjectValue(rs.getObject(i))
            }

            itemRec[attrName] = value
        }

        return itemRec
    }

    private fun parseObjectValue(value: Any?) = if (value is Clob) value.characterStream.readText() else value
}