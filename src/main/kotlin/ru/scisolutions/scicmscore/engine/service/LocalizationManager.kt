package ru.scisolutions.scicmscore.engine.service

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.LocaleService

@Service
class LocalizationManager(
    private val i18nProps: I18nProps,
    private val localeService: LocaleService
) {
    fun assignLocaleAttribute(item: Item, itemRec: ItemRec, locale: String?) {
        if (!item.localized) {
            itemRec.locale = null
            return
        }

        if (locale == null) {
            itemRec.locale = i18nProps.defaultLocale
        } else {
            if (!localeService.existsByName(locale)) {
                throw IllegalArgumentException("Locale [$locale] does not exist")
            }

            itemRec.locale = locale
        }
    }
}
