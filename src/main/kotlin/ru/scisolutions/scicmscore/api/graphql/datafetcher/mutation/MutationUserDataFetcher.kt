package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse

@DgsComponent
class MutationUserDataFetcher(private val engine: Engine) {
    @DgsMutation
    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse = engine.updateSessionData(sessionData)
}