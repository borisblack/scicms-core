package ru.scisolutions.scicmscore.engine.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import java.time.OffsetDateTime

@Service
class AuditManager(
    private val userService: UserService
) {
    fun assignAuditAttributes(item: Item, itemRec: ItemRec) {
        if (item.hasCreatedAtAttribute() || item.hasUpdatedAtAttribute()) {
            val now = OffsetDateTime.now()
            if (item.hasCreatedAtAttribute()) {
                itemRec.createdAt = now
            }

            if (item.hasUpdatedAtAttribute()) {
                itemRec.updatedAt = now
            }
        }

        if (item.hasCreatedByAttribute() || item.hasUpdatedByAttribute()) {
            val username = SecurityContextHolder.getContext().authentication.name
            val currentUser = userService.getByUsername(username)
            if (item.hasCreatedByAttribute()) {
                itemRec.createdBy = currentUser.id
            }

            if (item.hasUpdatedByAttribute()) {
                itemRec.updatedBy = currentUser.id
            }
        }
    }

    fun assignUpdateAttributes(item: Item, itemRec: ItemRec) {
        if (item.hasUpdatedAtAttribute()) {
            itemRec.updatedAt = OffsetDateTime.now()
        }

        if (item.hasUpdatedByAttribute()) {
            val username = SecurityContextHolder.getContext().authentication.name
            val currentUser = userService.getByUsername(username)
            itemRec.updatedBy = currentUser.id
        }
    }
}
