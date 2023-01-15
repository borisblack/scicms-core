package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_datasets")
class Dataset(
    @Column(nullable = false)
    var name: String,

    @Column(name = "data_source", nullable = false)
    var dataSource: String,

    @Column(name = "table_name")
    var tableName: String? = null,

    @Column(name = "query")
    var query: String? = null,

    @Column(nullable = false, name = "label_field")
    var labelField: String,

    @Column(nullable = false, name = "metric_type")
    var metricType: MetricType,

    @Column(nullable = false, name = "metric_field")
    var metricField: String,

    val unit: String? = null,

    @Column(name = "is_aggregate", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var isAggregate: Boolean = false,

    @Column(name = "aggregate_type")
    var aggregateType: AggregateType? = null,

    @Column(nullable = false, name = "temporal_type")
    var temporalType: TemporalType,

    @Column(name = "temporal_field")
    var temporalField: String? = null,

    @Column(name = "latitude_field")
    var latitudeField: String? = null,

    @Column(name = "longitude_field")
    var longitudeField: String? = null,

    @Column(name = "location_label_field")
    var locationLabelField: String? = null
) : AbstractEntity() {
    enum class MetricType {
        int, long, float, double, decimal, date, time, datetime, timestamp, bool
    }

    enum class TemporalType {
        date, time, datetime, timestamp
    }

    enum class AggregateType {
        countAll, count, sum, avg, min, max
    }

    fun getQueryOrThrow(): String {
        val q = if (query == null) null else "($query)"
        return tableName ?: q ?: throw IllegalStateException("Table name anq query are empty")
    }
}