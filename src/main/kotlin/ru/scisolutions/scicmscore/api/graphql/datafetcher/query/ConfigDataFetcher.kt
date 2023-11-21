package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.util.Schema

@DgsComponent
class ConfigDataFetcher(
    private val i18nProps: I18nProps,
    private val datasourceService: DatasourceService
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
            dataSources = setOf(Schema.MAIN_DATA_SOURCE_NAME) +
                datasourceService.findAll()
                    .map { it.name }
                    .toSet()
        ),
        i18n = i18nProps
    )
}