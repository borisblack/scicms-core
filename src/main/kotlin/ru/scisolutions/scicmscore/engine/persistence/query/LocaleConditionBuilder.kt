package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

@Component
class LocaleConditionBuilder(
    private val i18nProps: I18nProps,
) {
    fun newLocaleCondition(table: DbTable, item: Item, locale: String?): Condition? {
        val localeCol = DbColumn(table, LOCALE_COL_NAME, null, null)
        return if (item.localized) {
            if (locale == null) {
                ComboCondition(
                    ComboCondition.Op.OR,
                    BinaryCondition.equalTo(localeCol, i18nProps.defaultLocale),
                    UnaryCondition.isNull(localeCol),
                )
            } else if (locale != ALL_LOCALES) {
                BinaryCondition.equalTo(localeCol, locale)
            } else {
                null
            }
        } else {
            ComboCondition(
                ComboCondition.Op.OR,
                BinaryCondition.equalTo(localeCol, i18nProps.defaultLocale),
                UnaryCondition.isNull(localeCol),
            )
        }
    }

    companion object {
        private const val LOCALE_COL_NAME = "locale"
        private const val ALL_LOCALES = "all"
    }
}
