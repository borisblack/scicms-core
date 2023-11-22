package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.persistence.converter.MapConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_datasources")
class Datasource(
    @Column(nullable = false, unique = true)
    var name: String,

    @Column(name = "connection_string", nullable = false)
    var connectionString: String,

    @Column(nullable = false)
    var username: String,

    @Column(name = "passwd", nullable = false)
    val password: String,

    @Column(name = "max_pool_size")
    var maxPoolSize: Int?,

    @Column(name = "min_idle")
    var minIdle: Int?,

    @Convert(converter = MapConverter::class)
    var params: Map<String, Any?> = mapOf(),
) : AbstractEntity() {
    override fun toString(): String = "Datasource(name=$name)"

    companion object {
        const val MAIN_DATASOURCE_NAME = "main"
    }
}
