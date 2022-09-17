package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.db.Paginator
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.PermissionService
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToManyInversedBidirectionalRelation

@Component
class FindAllQueryBuilder(
    private val permissionService: PermissionService,
    private val filterConditionBuilder: FilterConditionBuilder,
    private val versionConditionBuilder: VersionConditionBuilder,
    private val stateConditionBuilder: StateConditionBuilder,
    private val localeConditionBuilder: LocaleConditionBuilder,
    private val paginator: Paginator,
    private val relationManager: RelationManager,
    private val orderingsParser: OrderingsParser
) {
    class FindAllQuery(
        val sql: String,
        val pagination: Pagination
    )

    fun buildFindAllQuery(item: Item, input: FindAllInput, selectAttrNames: Set<String>, selectPaginationFields: Set<String>, paramSource: AttributeSqlParameterSource): FindAllQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildFindAllInitialQuery(item, input.filters, selectAttrNames, schema, paramSource)
        val table = schema.findTable(item.tableName) ?: throw IllegalArgumentException("Table for currentItem is not found in schema")

        // Version
        val versionCondition = versionConditionBuilder.newVersionCondition(table, item, input.majorRev)
        if (versionCondition != null)
            query.addCondition(versionCondition)

        // Locale
        val localeCondition = localeConditionBuilder.newLocaleCondition(table, item, input.locale)
        if (localeCondition != null)
            query.addCondition(localeCondition)

        // State
        val stateCondition = stateConditionBuilder.newStateCondition(table, item, input.state)
        if (stateCondition != null)
            query.addCondition(stateCondition)

        val pagination = paginator.paginate(item, input.pagination, selectPaginationFields, query, paramSource)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            orderingsParser.parseOrderings(item, input.sort, schema, query, table)
        }

        return FindAllQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun buildFindAllInitialQuery(item: Item, filters: ItemFiltersInput?, selectAttrNames: Set<String>, schema: DbSchema, paramSource: AttributeSqlParameterSource): SelectQuery {
        val table = schema.addTable(item.tableName)

        val columns = selectAttrNames
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        val query = SelectQuery().addColumns(*columns)

        // Filters
        if (filters != null) {
            query.addCondition(
                filterConditionBuilder.newFilterCondition(item, filters, schema, table, query, paramSource)
            )
        }

        val permissionCondition = getPermissionCondition(table)
        query.addCondition(permissionCondition)

        return query.validate()
    }

    private fun getPermissionCondition(table: DbTable): Condition {
        val permissionIds = permissionService.findIdsForRead()
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
        parentId: String,
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
        val table = schema.findTable(item.tableName) ?: throw IllegalArgumentException("Table for currentItem is not found in schema")
        val parentAttribute = parentItem.spec.getAttributeOrThrow(parentAttrName)
        when (val parentRelation = relationManager.getAttributeRelation(parentItem, parentAttrName, parentAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                val owningCol = DbColumn(table, parentRelation.getOwningColumnName(), null, null)
                query.addCondition(BinaryCondition.equalTo(owningCol, parentId))
            }
            is ManyToManyRelation -> {
                val intermediateTable = schema.addTable(parentRelation.getIntermediateTableName())
                val sourceIntermediateCol =
                    DbColumn(intermediateTable, parentRelation.getIntermediateSourceColumnName(), null, null)
                val targetIntermediateCol =
                    DbColumn(intermediateTable, parentRelation.getIntermediateTargetColumnName(), null, null)
                val idCol = DbColumn(table, ItemRec.ID_COL_NAME, null, null)

                when (parentRelation) {
                    is ManyToManyUnidirectionalRelation -> {
                        query.addJoin(
                            SelectQuery.JoinType.LEFT_OUTER,
                            table,
                            intermediateTable,
                            BinaryCondition.equalTo(idCol, targetIntermediateCol)
                        )
                        query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentId))
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (parentRelation.isOwning) {
                            query.addJoin(
                                SelectQuery.JoinType.LEFT_OUTER,
                                table,
                                intermediateTable,
                                BinaryCondition.equalTo(idCol, targetIntermediateCol)
                            )
                            query.addCondition(BinaryCondition.equalTo(sourceIntermediateCol, parentId))
                        } else {
                            query.addJoin(
                                SelectQuery.JoinType.LEFT_OUTER,
                                table,
                                intermediateTable,
                                BinaryCondition.equalTo(idCol, sourceIntermediateCol)
                            )
                            query.addCondition(BinaryCondition.equalTo(targetIntermediateCol, parentId))
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Invalid relation")
        }

        val pagination = paginator.paginate(item, input.pagination, selectPaginationFields, query, paramSource)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            orderingsParser.parseOrderings(item, input.sort, schema, query, table)
        }

        return FindAllQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }
}