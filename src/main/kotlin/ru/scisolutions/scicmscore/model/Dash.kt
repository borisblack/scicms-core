package ru.scisolutions.scicmscore.model

import java.util.Objects
import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

data class Dash(
    val name: String,
    val type: String,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val refreshIntervalSeconds: Int = DEFAULT_REFRESH_INTERVAL_SECONDS,
    val metricType: AttrType,
    val temporalType: AttrType?,
    val datasets: List<Dataset> = emptyList()
) {

    companion object {
        private const val DEFAULT_REFRESH_INTERVAL_SECONDS: Int = 300
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dash

        return name == other.name &&
            type == other.type &&
            x == other.x &&
            y == other.y &&
            w == other.w &&
            h == other.h &&
            refreshIntervalSeconds == other.refreshIntervalSeconds &&
            metricType == other.metricType &&
            temporalType == other.temporalType &&
            datasets == other.datasets
    }

    override fun hashCode(): Int = Objects.hash(
        name,
        type,
        x,
        y,
        w,
        h,
        refreshIntervalSeconds,
        metricType,
        temporalType,
        datasets
    )
}