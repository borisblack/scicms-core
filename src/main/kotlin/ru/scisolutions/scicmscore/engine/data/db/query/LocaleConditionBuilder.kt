package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class LocaleConditionBuilder(
    @Value("\${scicms-core.i18n.default-locale}")
    private val defaultLocale: String
) {
    fun newLocaleCondition(table: DbTable, item: Item, locale: String?): Condition? {
        val localeCol = DbColumn(table, LOCALE_COL_NAME, null, null)
        return if (item.localized)  {
            if (locale == null) {
                ComboCondition(
                    ComboCondition.Op.OR,
                    BinaryCondition.equalTo(localeCol, defaultLocale),
                    UnaryCondition.isNull(localeCol)
                )
            } else if (locale != ALL_LOCALES) {
                BinaryCondition.equalTo(localeCol, locale)
            } else null
        } else {
            ComboCondition(
                ComboCondition.Op.OR,
                BinaryCondition.equalTo(localeCol, defaultLocale),
                UnaryCondition.isNull(localeCol)
            )
        }
    }

    companion object {
        private const val LOCALE_COL_NAME = "locale"
        private const val ALL_LOCALES = "all"
    }
}