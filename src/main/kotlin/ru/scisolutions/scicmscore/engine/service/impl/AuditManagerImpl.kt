package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.persistence.service.UserService
import java.time.OffsetDateTime
import java.util.UUID

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
            createdBy = UUID.fromString(currentUser.id)
            updatedAt = now
            updatedBy = UUID.fromString(currentUser.id)
        }
    }

    override fun assignUpdateAttributes(itemRec: ItemRec) {
        val username = SecurityContextHolder.getContext().authentication.name
        val currentUser = userService.getByUsername(username)

        with(itemRec) {
            updatedAt = OffsetDateTime.now()
            updatedBy = UUID.fromString(currentUser.id)
        }
    }
}