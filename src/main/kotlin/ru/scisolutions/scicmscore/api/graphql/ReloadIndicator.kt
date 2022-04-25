package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor.ReloadSchemaIndicator
import org.springframework.stereotype.Component

@Component
class ReloadIndicator : ReloadSchemaIndicator {
    override fun reloadSchema(): Boolean {
        return false
    }
}