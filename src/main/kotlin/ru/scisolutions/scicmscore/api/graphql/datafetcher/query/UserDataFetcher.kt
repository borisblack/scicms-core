package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.UserInfo

@DgsComponent
class UserDataFetcher(private val engine: Engine) {
    @DgsQuery
    fun me(): UserInfo? = engine.me()
}
