package ru.scisolutions.scicmscore.engine.hook.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.util.Json

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
            spec = parseSpec(data[Dataset::spec.name]),
            hash = data[Dataset::hash.name] as String?
        )

        if (datasetDao.actualizeSpec(dataset)) {
            data[Dataset::spec.name] = dataset.spec
            data[Dataset::hash.name] = dataset.hash
        }
    }

    private fun parseSpec(spec: Any?): DatasetSpec {
        if (spec == null)
            return DatasetSpec()

        if (spec is DatasetSpec)
            return spec

        if (spec is String)
            return objectMapper.readValue(spec, DatasetSpec::class.java)

        if (spec is Map<*, *>) {
            val specStr = objectMapper.writeValueAsString(spec)
            return objectMapper.readValue(specStr, DatasetSpec::class.java)
        }

        throw IllegalArgumentException("Unsupported spec type: ${spec::class.simpleName}")
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

    companion object {
        private val objectMapper = Json.objectMapper
    }
}