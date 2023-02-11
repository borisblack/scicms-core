package ru.scisolutions.scicmscore.persistence.service.impl

import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.PersistenceConfig.JdbcTemplateMap
import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.repository.DatasetRepository
import ru.scisolutions.scicmscore.persistence.service.DatasetService
import ru.scisolutions.scicmscore.util.Acl.Mask
import java.util.Objects

@Service
@Repository
@Transactional
class DatasetServiceImpl(
    private val jdbcTemplateMap: JdbcTemplateMap,
    private val datasetRepository: DatasetRepository
) : DatasetService {

    override fun getById(id: String): Dataset =
        datasetRepository.findById(id).orElseThrow { IllegalArgumentException("Dataset with ID [$id] not found") }

    override fun findByNameForRead(name: String): Dataset? =
        findByNameFor(name, Mask.READ)

    private fun findByNameFor(name: String, accessMask: Mask): Dataset? {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return datasetRepository.findByNameWithACL(name, accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }

    override fun actualizeSpec(dataset: Dataset) {
        val hash = Objects.hash(
            dataset.dataSource,
            dataset.getQueryOrThrow()
        ).toString()

        if (dataset.hash == hash)
            return

        dataset.spec = DatasetSpec(
            columns = columnsMapper.map(loadMetaData(dataset))
        )
        dataset.hash = hash
        datasetRepository.save(dataset)
    }

    private fun loadMetaData(dataset: Dataset): SqlRowSetMetaData {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(dataset.getQueryOrThrow())
        val query = SelectQuery()
            .addAllColumns()
            .addFromTable(table)
            .setFetchNext(1)
            .validate()

        val jdbcTemplate = jdbcTemplateMap.getOrThrow(dataset.dataSource)
        return jdbcTemplate.queryForRowSet(query.toString(), MapSqlParameterSource()).metaData
    }

    companion object {
        private val columnsMapper = ColumnsMapper()
    }
}