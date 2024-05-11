package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

@Component
class StateConditionBuilder {
    fun newStateCondition(table: DbTable, item: Item, state: String?): Condition? =
        if (state == null) {
            UnaryCondition.EMPTY
        } else {
            val stateCol = DbColumn(table, STATE_COL_NAME, null, null)
            BinaryCondition.equalTo(stateCol, state)
        }

    companion object {
        private const val STATE_COL_NAME = "state"
    }
}