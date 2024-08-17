package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.paginator.ItemPaginator
import ru.scisolutions.scicmscore.engine.persistence.service.PermissionService
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.service.RelationManager

@Component
class FindAllQueryBuilder(
    private val permissionService: PermissionService,
    private val itemFilterConditionBuilder: ItemFilterConditionBuilder,
    private val versionConditionBuilder: VersionConditionBuilder,
    private val stateConditionBuilder: StateConditionBuilder,
    private val localeConditionBuilder: LocaleConditionBuilder,
    private val itemPaginator: ItemPaginator,
    private val relationManager: RelationManager,
    private val orderingsParser: ItemOrderingsParser
) {
    class FindAllQuery(
        val sql: String,
        val pagination: Pagination
    )

    fun buildFindAllQuery(
        item: Item,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>,
        paramSource: AttributeSqlParameterSource
    ): FindAllQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildFindAllInitialQuery(item, input.filters, selectAttrNames, schema, paramSource)
        val table = schema.findTable(item.qs) ?: throw IllegalArgumentException("Query for currentItem is not found in schema")

        // Version
        val versionCondition = versionConditionBuilder.newVersionCondition(table, item, input.majorRev)
        if (versionCondition != null) {
            query.addCondition(versionCondition)
        }

        // Locale
        val localeCondition = localeConditionBuilder.newLocaleCondition(table, item, input.locale)
        if (localeCondition != null) {
            query.addCondition(localeCondition)
        }

        // State
        val stateCondition = stateConditionBuilder.newStateCondition(table, item, input.state)
        if (stateCondition != null) {
            query.addCondition(stateCondition)
        }

        val pagination = itemPaginator.paginate(item, input.pagination, selectPaginationFields, query, paramSource)

        // Sort
        if (input.sort.isNullOrEmpty()) {
            addDefaultOrdering(item, table, query)
        } else {
            orderingsParser.parseOrderings(item, input.sort, schema, table, query)
        }

        return FindAllQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun addDefaultOrdering(item: Item, table: DbTable, query: SelectQuery) {
        val defaultSortAttributeName = item.defaultSortAttribute ?: return

        val defaultSortAttribute = item.spec.getAttribute(defaultSortAttributeName)
        val defaultSortColumn = DbColumn(table, defaultSortAttribute.getColumnName(defaultSortAttributeName.lowercase()), null, null)
        query.addOrdering(defaultSortColumn, if (item.defaultSortAttribute?.lowercase() == "desc") Dir.DESCENDING else Dir.ASCENDING)
    }

    private fun buildFindAllInitialQuery(
        item: Item,
        filters: ItemFiltersInput?,
        selectAttrNames: Set<String>,
        schema: DbSchema,
        paramSource: AttributeSqlParameterSource
    ): SelectQuery {
        val table = schema.addTable(item.qs)

        val columns =
            selectAttrNames
                .map {
                    val attribute = item.spec.getAttribute(it)
                    DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
                }
                .toTypedArray()

        val query = SelectQuery().addColumns(*columns)

        // Filters
        if (filters != null) {
            query.addCondition(
                itemFilterConditionBuilder.newFilterCondition(item, filters, schema, table, query, paramSource)
            )
        }

        val permissionCondition = getPermissionCondition(table)
        query.addCondition(permissionCondition)

        return query.validate()
    }

    private fun getPermissionCondition(table: DbTable): Condition {
        val permissionIds = permissionService.idsForRead()
        val permissionIdCol = DbColumn(table, ItemRec.PERMISSION_COL_NAME, null, null)
        return if (permissionIds.isEmpty()) {
            UnaryCondition.isNull(permissionIdCol)
        } else {
            ComboCondition(
                ComboCondition.Op.OR,
                UnaryCondition.isNull(permissionIdCol),
                InCondition(permissionIdCol, *permissionIds.toTypedArray())
            )
        }
    }

    fun buildFindAllRelatedQuery(
        parentItem: Item,
        parentItemRec: ItemRec,
        parentAttrName: String,
        item: Item,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>,
        paramSource: AttributeSqlParameterSource
    ): FindAllQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildFindAllInitialQuery(item, input.filters, selectAttrNames, schema, paramSource)
        val table = schema.findTable(requireNotNull(item.tableName))
            ?: throw IllegalArgumentException("Table for item [${item.name}] not found in schema")
        val parentAttribute = parentItem.spec.getAttribute(parentAttrName)
        when (val parentRelation = relationManager.getAttributeRelation(parentItem, parentAttrName, parentAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                val owningCol = DbColumn(table, parentRelation.getOwningColumnName(), null, null)
                val parentKey = parentItemRec.asString(parentRelation.getOwningAttribute().referencedBy ?: parentItem.idAttribute)
                query.addCondition(BinaryCondition.equalTo(owningCol, parentKey))
            }
            is ManyToManyRelation -> {
                val intermediateTable = schema.addTable(parentRelation.getIntermediateTableName())
                val sourceIntermediateCol =
                    DbColumn(intermediateTable, parentRelation.getIntermediateSourceColumnName(), null, null)
                val targetIntermediateCol =
                    DbColumn(intermediateTable, parentRelation.getIntermediateTargetColumnName(), null, null)

                when (parentRelation) {
                    is ManyToManyUnidirectionalRelation -> {
                        val keyAttrName = parentRelation.getIntermediateTargetAttribute().referencedBy ?: item.idAttribute
                        val keyCol = DbColumn(table, item.spec.getColumnName(keyAttrName), null, null)
                        val parentKey =
                            parentItemRec.asString(
                                parentRelation.getIntermediateSourceAttribute().referencedBy ?: parentItem.idAttribute
                            )
                        query.addJoin(
                            SelectQuery.JoinType.LEFT_OUTER,
                            table,
                            intermediateTable,
                            BinaryCondition.equalTo(keyCol, targetIntermediateCol)
                        )
                        query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentKey))
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (parentRelation.isOwning) {
                            val keyAttrName = parentRelation.getIntermediateTargetAttribute().referencedBy ?: item.idAttribute
                            val keyCol = DbColumn(table, item.spec.getColumnName(keyAttrName), null, null)
                            val parentKey =
                                parentItemRec.asString(
                                    parentRelation.getIntermediateSourceAttribute().referencedBy ?: parentItem.idAttribute
                                )
                            query.addJoin(
                                SelectQuery.JoinType.LEFT_OUTER,
                                table,
                                intermediateTable,
                                BinaryCondition.equalTo(keyCol, targetIntermediateCol)
                            )
                            query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentKey))
                        } else {
                            val keyAttrName = parentRelation.getIntermediateSourceAttribute().referencedBy ?: item.idAttribute
                            val keyCol = DbColumn(table, item.spec.getColumnName(keyAttrName), null, null)
                            val parentKeyAttrName = parentRelation.getIntermediateTargetAttribute().referencedBy ?: parentItem.idAttribute
                            val parentKey = parentItemRec.asString(parentKeyAttrName)
                            query.addJoin(
                                SelectQuery.JoinType.LEFT_OUTER,
                                table,
                                intermediateTable,
                                BinaryCondition.equalTo(keyCol, sourceIntermediateCol)
                            )
                            query.addCondition(BinaryCondition.equalTo(targetIntermediateCol, parentKey))
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Invalid relation")
        }

        val pagination = itemPaginator.paginate(item, input.pagination, selectPaginationFields, query, paramSource)

        // Sort
        if (input.sort.isNullOrEmpty()) {
            addDefaultOrdering(item, table, query)
        } else {
            orderingsParser.parseOrderings(item, input.sort, schema, table, query)
        }

        return FindAllQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }
}
