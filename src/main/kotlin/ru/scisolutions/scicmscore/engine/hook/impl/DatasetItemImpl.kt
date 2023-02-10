package ru.scisolutions.scicmscore.engine.hook.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.persistence.service.DatasetService

@Service
class DatasetItemImpl(private val datasetService: DatasetService) : UpdateHook {
    override fun beforeUpdate(itemName: String, input: UpdateInput) {
        // Do nothing
    }

    override fun afterUpdate(itemName: String, response: Response) {
        val dataset = datasetService.getById(response.data?.id as String)
        datasetService.actualizeSpec(dataset)
        response.data["spec"] = dataset.spec
        response.data["hash"] = dataset.hash
    }
}