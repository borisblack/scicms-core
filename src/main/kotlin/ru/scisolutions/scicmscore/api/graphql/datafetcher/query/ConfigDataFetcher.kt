package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.model.SecurityConfigResponse
import ru.scisolutions.scicmscore.persistence.entity.Datasource
import ru.scisolutions.scicmscore.persistence.service.DatasourceService

@DgsComponent
class ConfigDataFetcher(
    private val i18nProps: I18nProps,
    private val securityProps: SecurityProps,
    private val datasourceService: DatasourceService
) {
    class Config(
        val data: DataConfigResponse,
        val i18n: I18ConfigResponse,
        val security: SecurityConfigResponse
    )

    class DataConfigResponse(
        val dataSources: Set<String>
    )

    class I18ConfigResponse(
        val defaultLocale: String
    )

    @DgsQuery
    fun config(): Config? = Config(
        data = DataConfigResponse(
            dataSources = setOf(Datasource.MAIN_DATASOURCE_NAME) +
                datasourceService.findAll()
                    .map { it.name }
                    .toSet()
        ),
        i18n = I18ConfigResponse(
            defaultLocale = i18nProps.defaultLocale
        ),
        security = SecurityConfigResponse(
            oauth2Providers = securityProps.oauth2Providers.map {
                SecurityConfigResponse.Oauth2ProviderConfigResponse(
                    id = it.id,
                    name = it.name,
                    authUrl = it.authUrl,
                    clientId = it.clientId
                )
            }
        )
    )
}