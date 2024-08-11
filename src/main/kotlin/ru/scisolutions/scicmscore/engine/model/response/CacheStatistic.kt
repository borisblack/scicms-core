package ru.scisolutions.scicmscore.engine.model.response

class CacheStatistic<T>(val result: T, val timeMs: Long, val cacheHit: Boolean)
