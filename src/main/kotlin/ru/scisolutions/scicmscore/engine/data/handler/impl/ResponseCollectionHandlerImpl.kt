package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.Paginator
import ru.scisolutions.scicmscore.engine.data.db.query.FilterConditionBuilder
import ru.scisolutions.scicmscore.engine.data.db.query.LocaleConditionBuilder
import ru.scisolutions.scicmscore.engine.data.db.query.OrderingsParser
import ru.scisolutions.scicmscore.engine.data.db.query.VersionConditionBuilder
import ru.scisolutions.scicmscore.engine.data.handler.ResponseCollectionHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.schema.relation.RelationManager
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.AccessUtil

@Service
class ResponseCollectionHandlerImpl(
    private val relationManager: RelationManager,
    private val itemService: ItemService,
    private val filterConditionBuilder: FilterConditionBuilder,
    private val localeConditionBuilder: LocaleConditionBuilder,
    private val versionConditionBuilder: VersionConditionBuilder,
    private val paginator: Paginator,
    private val jdbcTemplate: JdbcTemplate
) : ResponseCollectionHandler {
    override fun getResponseCollection(
        itemName: String,
        input: ResponseCollectionInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection {
        val item = itemService.getItemOrThrow(itemName)
        val attrNames = adjustAttrNames(item, selectAttrNames)

        // Query
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildFindAllQuery(schema, item, input, attrNames)
        val table = schema.findTable(item.tableName) ?: throw IllegalArgumentException("Table for currentItem is not found in schema")

        // Version
        val versionCondition = versionConditionBuilder.newVersionCondition(table, item, input.majorRev)
        if (versionCondition != null)
            query.addCondition(versionCondition)

        // Locale
        val localeCondition = localeConditionBuilder.newLocaleCondition(table, item, input.locale)
        if (localeCondition != null)
            query.addCondition(localeCondition)

        val pagination = paginator.paginate(query, input.pagination, selectPaginationFields)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            val orderings = orderingsParser.parseOrderings(item, input.sort)
            query.addCustomOrderings(*orderings.toTypedArray())
        }

        val sql = query.validate().toString()

        logger.debug("Running SQL: {}", sql)
        val itemRecList: List<ItemRec> = jdbcTemplate.query(sql, ItemRecMapper(item))

        return ResponseCollection(
            data = itemRecList,
            meta = ResponseCollectionMeta(
                pagination = pagination
            )
        )
    }

    override fun getRelationResponseCollection(
        parentItemName: String,
        itemName: String,
        sourceItemRec: ItemRec,
        attrName: String,
        input: ResponseCollectionInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): RelationResponseCollection {
        val item = itemService.getItemOrThrow(itemName)
        val attrNames = adjustAttrNames(item, selectAttrNames)

        // Query
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildFindAllQuery(schema, item, input, attrNames)
        val table = schema.findTable(item.tableName) ?: throw IllegalArgumentException("Table for currentItem is not found in schema")

        val parentItem = itemService.getItemOrThrow(parentItemName)
        val parentId = sourceItemRec[ID_ATTR_NAME] ?: IllegalArgumentException("Source ID not found")
        when (val parentRelation = relationManager.getAttributeRelation(parentItem, attrName)) {
            is OneToManyInversedBidirectionalRelation -> {
                val owningCol = DbColumn(table, parentRelation.getOwningColumnName(), null, null)
                query.addCondition(BinaryCondition.equalTo(owningCol, parentId))
            }
            is ManyToManyRelation -> {
                val intermediateTable = schema.addTable(parentRelation.getIntermediateTableName())
                val sourceIntermediateCol = DbColumn(intermediateTable, parentRelation.getIntermediateSourceColumnName(), null, null)
                val targetIntermediateCol = DbColumn(intermediateTable, parentRelation.getIntermediateTargetColumnName(), null, null)
                val idCol = DbColumn(table, ID_COL_NAME, null, null)

                when (parentRelation) {
                    is ManyToManyUnidirectionalRelation -> {
                        query.addJoin(SelectQuery.JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, targetIntermediateCol))
                        query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentId))
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (parentRelation.isOwning) {
                            query.addJoin(SelectQuery.JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, targetIntermediateCol))
                            query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentId))
                        } else {
                            query.addJoin(SelectQuery.JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, sourceIntermediateCol))
                            query.addCondition(BinaryCondition.equalTo(targetIntermediateCol, parentId))
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Invalid relation")
        }

        paginator.paginate(query, input.pagination, selectPaginationFields)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            val orderings = orderingsParser.parseOrderings(item, input.sort)
            query.addCustomOrderings(*orderings.toTypedArray())
        }

        val sql = query.validate().toString()

        logger.debug("Running SQL: {}", sql)
        val itemRecList: List<ItemRec> = jdbcTemplate.query(sql, ItemRecMapper(item))

        return RelationResponseCollection(
            data = itemRecList
        )
    }

    private fun adjustAttrNames(item: Item, selectAttrNames: Set<String>): Set<String> =
        selectAttrNames.asSequence()
            .filter {
                val attribute = item.spec.getAttributeOrThrow(it)
                !attribute.isCollection()
            }
            .plus(ID_ATTR_NAME)
            .toSet()

    private fun buildFindAllQuery(
        schema: DbSchema,
        item: Item,
        input: ResponseCollectionInput,
        selectAttrNames: Set<String>
    ): SelectQuery {
        val table = schema.addTable(item.tableName)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val columns = selectAttrNames
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        val query = SelectQuery()
            .addColumns(*columns)

        // Filters
        if (input.filters != null) {
            query.addCondition(
                filterConditionBuilder.newFilterCondition(schema, table, query, item, input.filters)
            )
        }

        return query
            .addCondition(
                InCondition(permissionIdCol, CustomSql(AccessUtil.getPermissionIdsForReadStatement()))
            )
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val ID_COL_NAME = "id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"

        private val logger = LoggerFactory.getLogger(ResponseCollectionHandlerImpl::class.java)
        private val orderingsParser = OrderingsParser()
    }
}