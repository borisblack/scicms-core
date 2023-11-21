package ru.scisolutions.scicmscore.util

object Schema {
    const val MAIN_DATA_SOURCE_NAME = "main"

    fun areDataSourcesEqual(sourceName: String?, targetName: String?): Boolean =
        (sourceName == targetName) ||
            (sourceName == null && targetName == MAIN_DATA_SOURCE_NAME) ||
            (sourceName == MAIN_DATA_SOURCE_NAME && targetName == null)
}