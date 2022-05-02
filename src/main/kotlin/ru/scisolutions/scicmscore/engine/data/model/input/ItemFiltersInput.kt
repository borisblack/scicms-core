package ru.scisolutions.scicmscore.engine.data.model.input

class ItemFiltersInput(
    val attributeFilters: Map<String, AbstractFilterInput<*>>,
    andFiltersList: List<ItemFiltersInput>?,
    orFiltersList: List<ItemFiltersInput>?,
    notFilters: ItemFiltersInput?
) : AbstractFilterInput<ItemFiltersInput>(andFiltersList, orFiltersList, notFilters)