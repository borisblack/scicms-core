package ru.scisolutions.scicmscore.engine.hook.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.UserItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.persistence.service.AccessService
import ru.scisolutions.scicmscore.persistence.service.IdentityService

@Service
class UserItemImpl(
    private val securityProps: SecurityProps,
    private val identityService: IdentityService,
    private val accessService: AccessService
) : DeleteHook {
    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        // Do nothing
    }

    override fun afterDelete(itemName: String, response: Response) {
        if (!securityProps.clearAccessOnUserDelete)
            return

        val userRec = UserItemRec(response.data as ItemRec)
        val username = requireNotNull(userRec.username)
        val identity = identityService.findByUsername(username) ?: return

        accessService.deleteAllByIdentityId(identity.id)
        identityService.deleteByUsername(username)
    }
}