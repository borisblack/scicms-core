package ru.scisolutions.scicmscore.entity

import ru.scisolutions.scicmscore.api.model.Spec
import ru.scisolutions.scicmscore.converter.SpecConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_items")
class Item(
    @Column(nullable = false)
    val name: String,

    @Column(name = "display_name")
    val displayName: String = name,

    @Column(name = "singular_name")
    val singularName: String = name,

    @Column(name = "plural_name", nullable = false)
    val pluralName: String,

    @Column(name = "table_name")
    val tableName: String = pluralName.lowercase(),

    @Column
    val description: String? = null,

    @Column(name = "data_source")
    val dataSource: String = DEFAULT_DATASOURCE,

    @Column(name = "icon")
    val icon: String? = null,

    @Column(name = "core")
    val core: Boolean = false,

    @Column(name = "perform_ddl")
    val performDdl: Boolean = false,

    @Column(name = "versioned")
    val versioned: Boolean = false,

    @Column(name = "manual_versioning")
    val manualVersioning: Boolean = false,

    @Column(name = "revision_policy_id")
    val revisionPolicyId: String? = null,

    @Column(name = "not_lockable")
    val notLockable: Boolean = false,

    @Column(name = "localized")
    val localized: Boolean = false,

    @Column(name = "implementation")
    val implementation: String? = null,

    @Column
    @Convert(converter = SpecConverter::class)
    var spec: Spec = Spec(),

    @Column
    val checksum: String? = null,

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
