package ru.scisolutions.scicmscore.engine.model.input

class DatasetFiltersInput(
    val fieldFilters: Map<String, PrimitiveFilterInput>,
    andFiltersList: List<DatasetFiltersInput>?,
    orFiltersList: List<DatasetFiltersInput>?,
    notFilters: DatasetFiltersInput?,
) : AbstractFilterInput<DatasetFiltersInput>(andFiltersList, orFiltersList, notFilters) {
    fun isEmpty(): Boolean = fieldFilters.isEmpty() && andFilterList.isNullOrEmpty() && orFilterList.isNullOrEmpty() && notFilter == null

    fun isNotEmpty(): Boolean = !isEmpty()
}
