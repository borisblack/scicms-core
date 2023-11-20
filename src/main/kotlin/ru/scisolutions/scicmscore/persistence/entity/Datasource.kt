package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.persistence.converter.MapConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_datasources")
class Datasource(
    @Column(nullable = false)
    var name: String,

    @Column(name = "connection_string", nullable = false)
    var connectionString: String,

    @Column(nullable = false)
    var username: String,

    @Column(name = "passwd", nullable = false)
    val password: String,

    @Column(name = "max_pool_size")
    var maxPoolSize: Int?,

    @Convert(converter = MapConverter::class)
    var params: Map<String, Any?> = mapOf(),
) : AbstractEntity() {
    override fun toString(): String = "ItemTemplate(name=$name)"

    companion object {
        const val DEFAULT_ITEM_TEMPLATE_NAME = "default"
    }
}
