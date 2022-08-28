package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.config.props.I18nProps

@DgsComponent
class ConfigDataFetcher(
    private val i18nProps: I18nProps
) {
    class Config(
        val i18n: I18nProps
    )

    @DgsQuery
    fun config(): Config? = Config(
        i18n = i18nProps
    )
}