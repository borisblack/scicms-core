package ru.scisolutions.scicmscore.engine.hook.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.itemrec.DatasourceItemRec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.service.DatasetService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.service.DatasourceManager

@Service
class DatasourceItemImpl(
    private val datasourceManager: DatasourceManager,
    private val itemService: ItemService,
    private val datasetService: DatasetService
) : CreateHook, UpdateHook, DeleteHook {
    override fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec): ItemRec? {
        checkConnection(DatasourceItemRec(data))
        return null
    }

    private fun checkConnection(ds: DatasourceItemRec) {
        datasourceManager.checkConnection(
            requireNotNull(ds.connectionString),
            requireNotNull(ds.username),
            requireNotNull(ds.password)
        )
    }

    override fun afterCreate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec): ItemRec? {
        checkConnection(DatasourceItemRec(data))
        return null
    }

    override fun afterUpdate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec) {
        val id = requireNotNull(data.id)
        if (itemService.existsByDatasourceId(id) || datasetService.existsByDatasourceId(id)) {
            throw IllegalArgumentException("Datasource is in use.")
        }
    }

    override fun afterDelete(itemName: String, response: Response) {
        // Do nothing
    }
}
