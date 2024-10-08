package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.engine.model.DatasetSpec
import ru.scisolutions.scicmscore.engine.persistence.converter.DatasetSpecConverter

@Entity
@Table(name = "bi_datasets")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
class Dataset(
    @Column(nullable = false)
    val name: String,
    var description: String? = null,
    @Column(name = "datasource_id")
    var datasourceId: String? = null,
    @ManyToOne
    @JoinColumn(name = "datasource_id", insertable = false, updatable = false)
    var datasource: Datasource? = null,
    @Column(name = "table_name")
    val tableName: String? = null,
    @Column(name = "query")
    val query: String? = null,
    @Column(name = "cache_ttl")
    var cacheTtl: Int? = null,
    @Convert(converter = DatasetSpecConverter::class)
    var spec: DatasetSpec = DatasetSpec(),
    var hash: String? = null
) : AbstractEntity() {
    val ds: String
        get() = datasource?.name ?: Datasource.MAIN_DATASOURCE_NAME

    val qs: String
        get() {
            val t = if (tableName.isNullOrBlank()) null else tableName
            val q = if (query.isNullOrBlank()) null else "($query)"

            return t ?: q ?: throw IllegalStateException("Table name anq query are empty.")
        }
}
