package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.LocalizationManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.LocaleService

@Service
class LocalizationManagerImpl(
    private val i18nProps: I18nProps,
    private val localeService: LocaleService
) : LocalizationManager {
    override fun assignLocaleAttribute(item: Item, itemRec: ItemRec, locale: String?) {
        if (!item.localized) {
            itemRec.locale = null
            return
        }

        if (locale == null) {
            itemRec.locale = i18nProps.defaultLocale
        } else {
            if (!localeService.existsByName(locale))
                throw IllegalArgumentException("Locale [$locale] does not exist")

            itemRec.locale = locale
        }
    }
}