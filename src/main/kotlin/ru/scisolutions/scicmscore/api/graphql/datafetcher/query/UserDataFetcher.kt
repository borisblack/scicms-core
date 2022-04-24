package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.UserInfo

@DgsComponent
class UserDataFetcher(
    private val dataEngine: DataEngine
) {
    @DgsQuery
    fun me(): UserInfo? = dataEngine.me()
}