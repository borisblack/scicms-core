package ru.scisolutions.scicmscore.engine.schema.model.relation

interface ManyToManyRelation : Relation {
    companion object {
        const val SOURCE_ATTR_NAME = "source"
        const val TARGET_ATTR_NAME = "target"
    }
}