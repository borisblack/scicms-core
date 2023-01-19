package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "core_datasets")
class Dataset(
    @Column(nullable = false)
    val name: String,

    @Column(name = "data_source", nullable = false)
    val dataSource: String,

    @Column(name = "table_name")
    val tableName: String? = null,

    @Column(name = "query")
    val query: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "metric_type")
    val metricType: MetricType,

    @Column(nullable = false, name = "metric_field")
    val metricField: String,

    val unit: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "temporal_type")
    val temporalType: TemporalType,

    @Column(name = "temporal_field")
    val temporalField: String? = null,

    @Column(name = "latitude_field")
    val latitudeField: String? = null,

    @Column(name = "longitude_field")
    val longitudeField: String? = null,

    @Column(name = "location_label_field")
    val locationLabelField: String? = null
) : AbstractEntity() {
    enum class MetricType {
        int, long, float, double, decimal, date, time, datetime, timestamp, bool
    }

    enum class TemporalType {
        date, time, datetime, timestamp
    }

    fun getQueryOrThrow(): String {
        val q = if (query == null) null else "($query)"
        return tableName ?: q ?: throw IllegalStateException("Table name anq query are empty")
    }
}