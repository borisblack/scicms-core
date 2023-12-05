package ru.scisolutions.scicmscore.engine.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.service.UserService
import java.time.OffsetDateTime

@Service
class AuditManager(
    private val userService: UserService
) {
    fun assignAuditAttributes(itemRec: ItemRec) {
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

    fun assignUpdateAttributes(itemRec: ItemRec) {
        val username = SecurityContextHolder.getContext().authentication.name
        val currentUser = userService.getByUsername(username)

        with(itemRec) {
            updatedAt = OffsetDateTime.now()
            updatedBy = currentUser.id
        }
    }
}