package ru.scisolutions.scicmscore.api.graphql

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class ExcludeItemPolicy {
    fun excludeFromQuery(item: Item) = item.name in excludedFromQueryItemNames

    fun excludeFromMutation(item: Item) = item.name in excludedFromMutationItemNames

    fun excludeFromCreateMutation(item: Item) = item.name in excludedFromCreateMutationItemNames

    fun excludeFromUpdateMutation(item: Item) = item.name in excludedFromUpdateMutationItemNames

    companion object {
        private val excludedFromQueryItemNames = setOf(Item.EXAMPLE_ITEM_NAME)
        private val excludedFromMutationItemNames = excludedFromQueryItemNames + setOf(/*Item.ITEM_TEMPLATE_ITEM_NAME, Item.ITEM_ITEM_NAME*/)
        private val excludedFromCreateMutationItemNames = setOf(Item.MEDIA_ITEM_NAME)
        private val excludedFromUpdateMutationItemNames = setOf<String>()
    }
}