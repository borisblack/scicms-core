package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset

class DatasetRlsRulesBuilder {
    /**
     * Adds RLS restrictions into SQL query.
     */
    fun addRlsRules(dataset: Dataset, table: DbTable, input: DatasetInput, query: SelectQuery) {
        val activeRls = dataset.spec.activeRls
        if (activeRls.isEmpty()) return

        val authentication = SecurityContextHolder.getContext().authentication ?: throw AccessDeniedException("User is not authenticated.")
        val authorities = AuthorityUtils.authorityListToSet(authentication.authorities)
        val fields = input.fields?.associateBy { it.name }
            ?: dataset.spec.columns.mapValues { (colName, _) -> datasetSqlExprEvaluator.toFieldInput(dataset, colName) }

        val falseCond = CustomSql("0 = 1")

        // Process RLS entries
        for ((colName, rlsEntries) in activeRls) {
            val field = fields[colName] ?: continue
            val type = datasetSqlExprEvaluator.calculateType(dataset.spec.columns, field)
            if (type != FieldType.string)
                throw IllegalArgumentException("Only string column types are supported in RLS (actual type of column [$colName] is [$type]).")

            var colWhereCond: InCondition? = null
            var colHavingCond: InCondition? = null
            for (rlsEntry in rlsEntries) {
                rlsEntry.validate(colName) // validate RLS entry

                if (rlsEntry.anyIdentity || authentication.name in rlsEntry.users || rlsEntry.roles.any { it in authorities }) {
                    if (rlsEntry.anyValue) {
                        // No restrictions for current column
                        colWhereCond = null
                        colHavingCond = null
                        break
                    }

                    if (datasetSqlExprEvaluator.isAggregate(field)) {
                        if (colHavingCond == null) {
                            val col = DbColumn(table, colName, null, null)
                            colHavingCond = InCondition(col, rlsEntry.value)
                        } else {
                            colHavingCond.addObject(rlsEntry.value)
                        }
                    } else {
                        if (colWhereCond == null) {
                            val col = DbColumn(table, colName, null, null)
                            colWhereCond = InCondition(col, rlsEntry.value)
                        } else {
                            colWhereCond.addObject(rlsEntry.value)
                        }
                    }
                }
            }

            if (colWhereCond != null) {
                query.addCondition(ComboCondition(ComboCondition.Op.OR, falseCond, colWhereCond))
            }

            if (colHavingCond != null) {
                query.addCondition(ComboCondition(ComboCondition.Op.OR, falseCond, colHavingCond))
            }
        }
    }

    companion object {
        private val datasetSqlExprEvaluator = DatasetSqlExprEvaluator()
    }
}