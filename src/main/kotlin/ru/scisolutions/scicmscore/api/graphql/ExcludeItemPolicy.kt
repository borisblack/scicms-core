package ru.scisolutions.scicmscore.api.graphql

import ru.scisolutions.scicmscore.persistence.entity.Item

class ExcludeItemPolicy {
    fun excludeFromQuery(item: Item) = item.name in excludedFromQueryItemNames

    fun excludeFromMutation(item: Item) = item.name in excludedMutationItemNames

    fun excludeFromCreateMutation(item: Item) = item.name in excludedFromCreateMutationItemNames

    fun excludeFromUpdateMutation(item: Item) = item.name in excludedFromUpdateMutationItemNames

    companion object {
        private const val EXAMPLE_ITEM_NAME = "example"
        private const val ITEM_ITEM_NAME = "item"
        private const val MEDIA_ITEM_NAME = "media"

        private val excludedFromQueryItemNames = setOf(EXAMPLE_ITEM_NAME)
        private val excludedMutationItemNames = excludedFromQueryItemNames.plus(setOf(ITEM_ITEM_NAME))
        private val excludedFromCreateMutationItemNames = setOf(MEDIA_ITEM_NAME)
        private val excludedFromUpdateMutationItemNames = setOf(MEDIA_ITEM_NAME)
    }
}