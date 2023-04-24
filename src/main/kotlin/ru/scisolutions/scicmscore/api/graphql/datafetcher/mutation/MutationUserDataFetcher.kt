package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import ru.scisolutions.scicmscore.engine.Engine

@DgsComponent
class MutationUserDataFetcher(private val engine: Engine) {
    @DgsMutation
    fun updateSessionData(sessionData: Map<String, Any?>?): Map<String, Any?>? = engine.updateSessionData(sessionData)
}