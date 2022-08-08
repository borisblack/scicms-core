package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor.ReloadSchemaIndicator
import org.springframework.stereotype.Component

@Component
class ReloadIndicator : ReloadSchemaIndicator {
    @Volatile
    private var isNeedReloadOnce = false

    fun setNeedReloadOnce(isNeedReloadOnce: Boolean) {
        this.isNeedReloadOnce = isNeedReloadOnce
    }

    override fun reloadSchema(): Boolean {
        if (isNeedReloadOnce) {
            isNeedReloadOnce = false
            return true
        }

        return false
    }
}