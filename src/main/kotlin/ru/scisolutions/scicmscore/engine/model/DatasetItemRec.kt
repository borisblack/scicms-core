package ru.scisolutions.scicmscore.engine.model

class DatasetItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map) {
    var datasource: String?
        get() = this[DATASOURCE_ATTR_NAME] as String?
        set(value) { this[DATASOURCE_ATTR_NAME] = value }

    companion object {
        const val DATASOURCE_ATTR_NAME = "datasource"
    }
}