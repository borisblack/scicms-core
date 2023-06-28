package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.config.props.I18nProps

@DgsComponent
class ConfigDataFetcher(
    private val dataProps: DataProps,
    private val i18nProps: I18nProps
) {
    class Config(
        val data: DataConfig,
        val i18n: I18nProps
    )

    class DataConfig(
        val dataSources: Set<String>
    )

    @DgsQuery
    fun config(): Config? = Config(
        data = DataConfig(
            dataSources = dataProps.dataSources.keys
        ),
        i18n = i18nProps
    )
}