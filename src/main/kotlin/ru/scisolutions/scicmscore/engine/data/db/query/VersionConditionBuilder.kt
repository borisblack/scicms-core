package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class VersionConditionBuilder {
    fun newVersionCondition(table: DbTable, item: Item, majorRev: String?): Condition? {
        val currentCol = DbColumn(table, IS_CURRENT_COL_NAME, null, null)
        return if (item.versioned)  {
            if (majorRev == null) {
                BinaryCondition.equalTo(currentCol, true)
            } else if (majorRev != ALL_VERSIONS) {
                val majorRevCol = DbColumn(table, MAJOR_REV_COL_NAME, null, null)
                BinaryCondition.equalTo(majorRevCol, majorRev)
            } else null
        } else {
            BinaryCondition.equalTo(currentCol, true)
        }
    }

    companion object {
        private const val IS_CURRENT_COL_NAME = "is_current"
        private const val MAJOR_REV_COL_NAME = "major_rev"
        private const val ALL_VERSIONS = "all"
    }
}