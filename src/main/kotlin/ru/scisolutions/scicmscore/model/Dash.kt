package ru.scisolutions.scicmscore.model

import java.util.Objects

data class Dash(
    val name: String,
    val type: String,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val dataset: String,
    val labelField: String,
    val isAggregate: Boolean = false,
    val aggregateType: AggregateType? = null,
    val refreshIntervalSeconds: Int = DEFAULT_REFRESH_INTERVAL_SECONDS
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
            dataset == other.dataset &&
            labelField == other.labelField &&
            isAggregate == other.isAggregate &&
            aggregateType == other.aggregateType &&
            refreshIntervalSeconds == other.refreshIntervalSeconds
    }

    override fun hashCode(): Int = Objects.hash(
        name,
        type,
        x,
        y,
        w,
        h,
        dataset,
        labelField,
        isAggregate,
        aggregateType,
        refreshIntervalSeconds
    )
}