package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.service.UserService
import java.time.OffsetDateTime

@Service
class AuditManagerImpl(
    private val userService: UserService
) : AuditManager {
    override fun assignAuditAttributes(itemRec: ItemRec) {
        val now = OffsetDateTime.now()
        val username = SecurityContextHolder.getContext().authentication.name
        val currentUser = userService.getByUsername(username)

        with(itemRec) {
            createdAt = now
            createdBy = currentUser.id
            updatedAt = now
            updatedBy = currentUser.id
        }
    }

    override fun assignUpdateAttributes(itemRec: ItemRec) {
        val username = SecurityContextHolder.getContext().authentication.name
        val currentUser = userService.getByUsername(username)

        with(itemRec) {
            updatedAt = OffsetDateTime.now()
            updatedBy = currentUser.id
        }
    }
}