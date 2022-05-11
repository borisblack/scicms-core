package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ReleasedConditionBuilder {
    fun newReleasedCondition(table: DbTable, item: Item, isReleased: Boolean?): Condition? {
        val releasedCol = DbColumn(table, RELEASED_COL_NAME, null, null)
        return when (isReleased) {
            null -> BinaryCondition.equalTo(releasedCol, true)
            true -> BinaryCondition.equalTo(releasedCol, true)
            else -> {
                ComboCondition(
                    ComboCondition.Op.OR,
                    BinaryCondition.equalTo(releasedCol, false),
                    UnaryCondition.isNull(releasedCol)
                )
            }
        }
    }

    companion object {
        private const val RELEASED_COL_NAME = "released"
    }
}