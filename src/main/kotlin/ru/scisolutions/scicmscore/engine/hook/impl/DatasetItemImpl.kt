package ru.scisolutions.scicmscore.engine.hook.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.dao.DatasourceDao
import ru.scisolutions.scicmscore.engine.db.mapper.ColumnsMapper
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.DatasetItemRec
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.util.Json
import java.util.Objects

@Service
class DatasetItemImpl(
    private val datasourceDao: DatasourceDao,
    private val datasourceService: DatasourceService
) : CreateHook, UpdateHook {
    override fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec) {
        actualizeSpec(DatasetItemRec(data))
    }

    private fun actualizeSpec(data: DatasetItemRec) {
        val datasource = data.datasource?.let { datasourceService.getById(it) }
        val dataset = Dataset(
            name = data[Dataset::name.name] as String,
            description = data[Dataset::description.name] as String?,
            datasourceId = datasource?.id,
            datasource = datasource,
            tableName = data[Dataset::tableName.name] as String?,
            query = data[Dataset::query.name] as String?,
            spec = parseSpec(data[Dataset::spec.name]),
            hash = data[Dataset::hash.name] as String?
        )

        if (actualizeSpec(dataset)) {
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

        if (spec is Map<*, *>)
            return objectMapper.convertValue(spec, DatasetSpec::class.java)

        throw IllegalArgumentException("Unsupported spec type: ${spec::class.simpleName}")
    }

    private fun actualizeSpec(dataset: Dataset): Boolean {
        val hash = Objects.hash(
            dataset.ds,
            dataset.qs
        ).toString()

        if (dataset.hash == hash) {
            logger.debug("Dataset has not changed. Skip actualizing")
            return false
        }

        logger.debug("Dataset has changed. Reloading meta")
        dataset.spec = DatasetSpec(
            columns = columnsMapper.map(dataset, datasourceDao.loadMetaData(dataset.ds, dataset.qs))
        )
        dataset.hash = hash

        return true
    }

    override fun afterCreate(itemName: String, response: Response) {
        // Do nothing
    }

    override fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec) {
        actualizeSpec(DatasetItemRec(data))
    }

    override fun afterUpdate(itemName: String, response: Response) {
        // Do nothing
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasetItemImpl::class.java)
        private val objectMapper = Json.objectMapper
        private val columnsMapper = ColumnsMapper()
    }
}