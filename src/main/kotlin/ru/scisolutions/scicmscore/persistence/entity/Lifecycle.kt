package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.model.LifecycleSpec
import ru.scisolutions.scicmscore.persistence.converter.LifecycleSpecConverter
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_lifecycles")
class Lifecycle(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String? = name,

    var description: String? = null,
    var icon: String? = null,

    @Column(name = "start_state", nullable = false)
    var startState: String,

    var implementation: String? = null,

    @Convert(converter = LifecycleSpecConverter::class)
    var spec: LifecycleSpec = LifecycleSpec(),

    var checksum: String? = null,
    var hash: String? = null,
) : AbstractEntity() {
    override fun toString(): String = "Lifecycle(name=$name)"

    companion object {
        val DEFAULT_LIFECYCLE_ID: UUID = UUID.fromString("ad051120-65cf-440a-8fc3-7a24ac8301d3")
    }
}
