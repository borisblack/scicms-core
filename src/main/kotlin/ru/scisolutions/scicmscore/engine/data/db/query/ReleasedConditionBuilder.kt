package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ReleasedConditionBuilder {
    fun newReleasedCondition(table: DbTable, item: Item, isReleased: Boolean?): Condition? {
        val currentCol = DbColumn(table, RELEASED_COL_NAME, null, null)
        return if (isReleased == null)
            BinaryCondition.equalTo(currentCol, true)
        else
            BinaryCondition.equalTo(currentCol, isReleased)
    }

    companion object {
        private const val RELEASED_COL_NAME = "released"
    }
}