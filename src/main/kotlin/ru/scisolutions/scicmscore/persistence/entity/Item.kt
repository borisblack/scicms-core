package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import ru.scisolutions.scicmscore.domain.model.ItemSpec
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
    var displayName: String? = name,

    @Column(name = "singular_name")
    var singularName: String? = name,

    @Column(name = "plural_name", nullable = false)
    var pluralName: String,

    @Column(name = "table_name")
    var tableName: String = pluralName.lowercase(),

    @Column
    var description: String? = null,

    @Column(name = "data_source")
    var dataSource: String,

    @Column(name = "icon")
    var icon: String? = null,

    @Column(name = "core", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var core: Boolean = false,

    @Column(name = "perform_ddl", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var performDdl: Boolean = false,

    @Column(name = "versioned", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var versioned: Boolean = false,

    @Column(name = "manual_versioning", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var manualVersioning: Boolean = false,

    @Column(name = "revision_policy_id")
    var revisionPolicyId: String? = null,

    @Column(name = "not_lockable", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var notLockable: Boolean = false,

    @Column(name = "localized", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var localized: Boolean = false,

    @Column(name = "implementation")
    var implementation: String? = null,

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
    override fun toString(): String = "Item(name=[$name])"
}
