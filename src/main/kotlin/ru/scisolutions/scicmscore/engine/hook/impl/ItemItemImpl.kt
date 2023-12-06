package ru.scisolutions.scicmscore.engine.hook.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.model.ItemItemRec
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.schema.service.TableSeeder

@Service
class ItemItemImpl(
    private val itemService: ItemService,
    private val tableSeeder: TableSeeder
) : DeleteHook {

    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        itemService.findByIdForDelete(input.id)
            ?: throw AccessDeniedException("You are not allowed to delete item with id [${input.id}].")
    }

    override fun afterDelete(itemName: String, response: Response) {
        val itemItemRec = ItemItemRec(response.data as ItemRec)
        if (itemItemRec.performDdl != true) {
            logger.info("DDL performing flag is disabled for item [{}]. Deleting skipped.", itemItemRec.name)
            return
        }

        tableSeeder.dropTable(
            requireNotNull(itemItemRec.dataSource),
            requireNotNull(itemItemRec.tableName)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemItemImpl::class.java)
    }
}