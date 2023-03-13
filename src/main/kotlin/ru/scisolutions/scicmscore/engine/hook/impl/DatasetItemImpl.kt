package ru.scisolutions.scicmscore.engine.hook.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Service
class DatasetItemImpl(private val datasetDao: DatasetDao) : CreateHook, UpdateHook {
    override fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec) {
        actualizeSpec(data)
    }

    private fun actualizeSpec(data: ItemRec) {
        val dataset = Dataset(
            name = data[Dataset::name.name] as String,
            description = data[Dataset::description.name] as String?,
            dataSource = data[Dataset::dataSource.name] as String,
            tableName = data[Dataset::tableName.name] as String?,
            query = data[Dataset::query.name] as String?,
            hash = data[Dataset::hash.name] as String?
        )
        datasetDao.actualizeSpec(dataset)
        data[Dataset::spec.name] = dataset.spec
        data[Dataset::hash.name] = dataset.hash
    }

    override fun afterCreate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec) {
        actualizeSpec(data)
    }

    override fun afterUpdate(itemName: String, response: Response) {
        // Do nothing
    }
}