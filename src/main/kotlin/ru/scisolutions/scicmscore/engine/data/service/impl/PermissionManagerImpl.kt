package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Permission

@Service
class PermissionManagerImpl : PermissionManager {
    override fun assignPermissionAttribute(itemRec: ItemRec) {
        if (itemRec.permission == null)
            itemRec.permission = Permission.DEFAULT_PERMISSION_ID
    }
}