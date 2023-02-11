package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.persistence.converter.DatasetSpecConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
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

    @Convert(converter = DatasetSpecConverter::class)
    var spec: DatasetSpec = DatasetSpec(),

    var hash: String? = null
) : AbstractEntity() {
    fun getQueryOrThrow(): String {
        val t = if (tableName.isNullOrBlank()) null else tableName
        val q = if (query.isNullOrBlank()) null else "($query)"

        return t ?: q ?: throw IllegalStateException("Table name anq query are empty")
    }
}