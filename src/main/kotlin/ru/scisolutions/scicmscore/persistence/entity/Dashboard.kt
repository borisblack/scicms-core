package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.model.DashboardSpec
import ru.scisolutions.scicmscore.persistence.converter.DashboardSpecConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_dashboards")
class Dashboard(
    @Column(nullable = false)
    var name: String,

    @Convert(converter = DashboardSpecConverter::class)
    var spec: DashboardSpec,

    var checksum: String? = null,
    var hash: String? = null
) : AbstractEntity() {
    override fun toString(): String = "Dashboard(name=$name)"
}
