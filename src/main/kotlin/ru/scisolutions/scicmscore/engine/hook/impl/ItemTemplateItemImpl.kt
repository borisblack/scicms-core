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
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemTemplateItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.engine.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemTemplateMapper
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult

@Service
class ItemTemplateItemImpl(
    private val itemTemplateService: ItemTemplateService,
    private val schemaLockService: SchemaLockService,
    private val itemTemplateMapper: ItemTemplateMapper,
    private val modelsApplier: ModelsApplier,
    private val reloadIndicator: ReloadIndicator
) : CreateHook, UpdateHook, DeleteHook {
    override fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec): ItemRec? {
        val appliedModelResult = apply(ItemTemplateItemRec(data))
        data.id = appliedModelResult.id
        data.configId = appliedModelResult.id
        return data
    }

    private fun apply(data: ItemTemplateItemRec): ModelApplyResult {
        val model = itemTemplateMapper.mapToModel(data)

        schemaLockService.lockOrThrow()
        val appliedModelResult = modelsApplier.apply(model, true)
        schemaLockService.unlockOrThrow()

        return appliedModelResult
    }

    override fun afterCreate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec): ItemRec? {
        val itemTemplateItemRec = ItemTemplateItemRec(data)
        val existingItemTemplate = itemTemplateService.getById(input.id)
        if (itemTemplateItemRec.name != existingItemTemplate.name) {
            throw IllegalArgumentException("Item template cannot be renamed.")
        }

        apply(itemTemplateItemRec)
        return data
    }

    override fun afterUpdate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        // Do nothing
    }

    override fun afterDelete(itemName: String, response: Response) {
        reloadIndicator.setNeedReload(true)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemTemplateItemImpl::class.java)
    }
}
