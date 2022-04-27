package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.engine.schema.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.converter.ItemImplementationConverter
import ru.scisolutions.scicmscore.persistence.converter.ItemSpecConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_items")
class Item(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String = name,

    @Column(name = "singular_name")
    var singularName: String = name,

    @Column(name = "plural_name", nullable = false)
    var pluralName: String,

    @Column(name = "table_name")
    var tableName: String = pluralName.lowercase(),

    @Column
    var description: String? = null,

    @Column(name = "data_source")
    var dataSource: String = DEFAULT_DATASOURCE,

    @Column(name = "icon")
    var icon: String? = null,

    @Column(name = "core")
    var core: Boolean = false,

    @Column(name = "perform_ddl")
    var performDdl: Boolean = false,

    @Column(name = "versioned")
    var versioned: Boolean = false,

    @Column(name = "manual_versioning")
    var manualVersioning: Boolean = false,

    @Column(name = "revision_policy_id")
    var revisionPolicyId: String? = null,

    @Column(name = "not_lockable")
    var notLockable: Boolean = false,

    @Column(name = "localized")
    var localized: Boolean = false,

    @Column(name = "implementation")
    @Convert(converter = ItemImplementationConverter::class)
    var implementation: Class<*>? = null,

    @Column
    @Convert(converter = ItemSpecConverter::class)
    var spec: ItemSpec = ItemSpec(),

    @Column
    var checksum: String? = null,

    // @ManyToMany(cascade = [CascadeType.ALL])
    // @JoinTable(
    //     name = "sec_allowed_permissions",
    //     joinColumns = [JoinColumn(name = "source_id")],
    //     inverseJoinColumns = [JoinColumn(name = "target_id")],
    // )
    // val allowedPermissions: MutableSet<Permission> = mutableSetOf()
) : AbstractEntity() {
    companion object {
        const val DEFAULT_DATASOURCE = "main"
    }
}
