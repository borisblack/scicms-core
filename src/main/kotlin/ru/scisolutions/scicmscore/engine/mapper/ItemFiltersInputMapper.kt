package ru.scisolutions.scicmscore.engine.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.AND_KEY
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.NOT_KEY
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.OR_KEY
import ru.scisolutions.scicmscore.engine.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.TypedPrimitiveFilterInput
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.schema.service.RelationValidator

@Component
class ItemFiltersInputMapper(
    private val itemCache: ItemCache,
    private val relationValidator: RelationValidator
) {
    fun map(itemName: String, itemFiltersMap: Map<String, Any>, opPrefix: String = ""): ItemFiltersInput {
        val item = itemCache.getOrThrow(itemName)
        return map(item, itemFiltersMap, opPrefix)
    }

    private fun map(item: Item, itemFiltersMap: Map<String, Any>, opPrefix: String = ""): ItemFiltersInput {
        val itemFiltersMapOfMaps = itemFiltersMap.filterValues { it is Map<*, *> } as Map<String, Map<String, Any>>
        val itemFiltersMapOfLists = itemFiltersMap.filterValues { it is List<*> } as Map<String, List<Any>>

        val attributeFilters: Map<String, AbstractFilterInput<*>> =
            itemFiltersMapOfMaps
                .filterKeys { it !in excludedKeys }
                .mapValues { (attrName, filterValue) ->
                    val attribute = item.spec.getAttributeOrThrow(attrName)
                    if (attribute.type == FieldType.media) {
                        val media = itemCache.getMedia()
                        if (media.ds == item.ds)
                            map(MEDIA_ITEM_NAME, filterValue, opPrefix)
                        else
                            TypedPrimitiveFilterInput.fromMap(attribute.type, filterValue)
                    } else if (attribute.type == FieldType.relation) {
                        relationValidator.validateAttribute(item, attrName, attribute)
                        val targetItem = itemCache.getOrThrow(requireNotNull(attribute.target))
                        if (targetItem.ds == item.ds) {
                            map(attribute.target, filterValue, opPrefix) // recursive
                        } else {
                            if (attribute.isCollection())
                                throw IllegalArgumentException("Filtering collections from different datasource is not supported")

                            TypedPrimitiveFilterInput.fromMap(attribute.type, filterValue)
                        }
                    } else TypedPrimitiveFilterInput.fromMap(attribute.type, filterValue)
                }

        val andFiltersList = itemFiltersMapOfLists["${opPrefix}$AND_KEY"]?.let { list ->
            list.filterIsInstance<Map<*, *>>()
                .map { map(item, it as Map<String, Any>, opPrefix) } // recursive
        }

        val orFiltersList = itemFiltersMapOfLists["${opPrefix}$OR_KEY"]?.let { list ->
            list.filterIsInstance<Map<*, *>>()
                .map { map(item, it as Map<String, Any>, opPrefix) } // recursive
        }

        val notFilters = itemFiltersMapOfMaps["${opPrefix}$NOT_KEY"]?.let { map(item, it, opPrefix) } // recursive

        return ItemFiltersInput(
            attributeFilters = attributeFilters,
            andFiltersList = andFiltersList,
            orFiltersList = orFiltersList,
            notFilters = notFilters
        )
    }

    companion object {
        private const val MEDIA_ITEM_NAME = "media"
        private val excludedKeys = setOf(AND_KEY, OR_KEY, NOT_KEY)
    }
}