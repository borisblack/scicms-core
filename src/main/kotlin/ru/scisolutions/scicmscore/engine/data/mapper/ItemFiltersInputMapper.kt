package ru.scisolutions.scicmscore.engine.data.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.model.input.AbstractFilterInput
import ru.scisolutions.scicmscore.engine.data.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.data.model.input.PrimitiveFilterInput
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class ItemFiltersInputMapper(private val itemService: ItemService) {
    fun map(itemName: String, itemFiltersMap: Map<String, Any>): ItemFiltersInput {
        val item = itemService.getItemOrThrow(itemName)
        return map(item, itemFiltersMap)
    }

    private fun map(item: Item, itemFiltersMap: Map<String, Any>): ItemFiltersInput {
        val itemFiltersMapOfMaps = itemFiltersMap.filterValues { it is Map<*, *> } as Map<String, Map<String, Any>>
        val itemFiltersMapOfLists = itemFiltersMap.filterValues { it is List<*> } as Map<String, List<Any>>

        val attributeFilters: Map<String, AbstractFilterInput<*>> =
            itemFiltersMapOfMaps
                .filterKeys { it !in excludedKeys }
                .mapValues { (attrName, filterValue) ->
                    val attribute = item.spec.getAttributeOrThrow(attrName)
                    if (attribute.type == Type.relation)
                        map(attribute.extractTarget(), filterValue) // recursive
                    else
                        PrimitiveFilterInput.fromMap(filterValue)
                }

        val andFiltersList = itemFiltersMapOfLists[AbstractFilterInput.AND_KEY]?.let { list ->
            list.filterIsInstance<Map<*, *>>()
                .map { map(item, it as Map<String, Any>) } // recursive
        }

        val orFiltersList = itemFiltersMapOfLists[AbstractFilterInput.OR_KEY]?.let { list ->
            list.filterIsInstance<Map<*, *>>()
                .map { map(item, it as Map<String, Any>) } // recursive
        }

        val notFilters = itemFiltersMapOfMaps[AbstractFilterInput.NOT_KEY]?.let { map(item, it) } // recursive

        return ItemFiltersInput(
            attributeFilters = attributeFilters,
            andFiltersList = andFiltersList,
            orFiltersList = orFiltersList,
            notFilters = notFilters
        )
    }

    companion object {
        private val excludedKeys = setOf(AbstractFilterInput.AND_KEY, AbstractFilterInput.OR_KEY, AbstractFilterInput.NOT_KEY)
    }
}