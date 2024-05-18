package ru.scisolutions.scicmscore.engine.hook.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.api.graphql.ReloadIndicator
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemItemRec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.ItemMetadata
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult
import ru.scisolutions.scicmscore.engine.schema.service.TableSeeder

@Service
class ItemItemImpl(
    private val itemMapper: ItemMapper,
    private val modelsApplier: ModelsApplier,
    private val tableSeeder: TableSeeder,
    private val reloadIndicator: ReloadIndicator,
) : CreateHook, UpdateHook, DeleteHook {
    override fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec): ItemRec? {
        val appliedModelResult = apply(ItemItemRec(data))
        data.id = appliedModelResult.id
        data.configId = appliedModelResult.id
        return data
    }

    private fun apply(data: ItemItemRec): ModelApplyResult {
        val model = itemMapper.mapToModel(data)
        return modelsApplier.apply(model, true)
    }

    override fun afterCreate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec): ItemRec? {
        apply(ItemItemRec(data))
        return data
    }

    override fun afterUpdate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        // Do nothing
    }

    override fun afterDelete(itemName: String, response: Response) {
        val itemItemRec = ItemItemRec(response.data as ItemRec)
        if (itemItemRec.performDdl == true) {
            tableSeeder.dropTable(
                itemItemRec.dataSource ?: ItemMetadata.MAIN_DATASOURCE_NAME,
                requireNotNull(itemItemRec.tableName)
            )
        } else {
            logger.info("DDL performing flag is disabled for item [{}]. Deleting skipped.", itemItemRec.name)
        }

        reloadIndicator.setNeedReload(true)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemItemImpl::class.java)
    }
}