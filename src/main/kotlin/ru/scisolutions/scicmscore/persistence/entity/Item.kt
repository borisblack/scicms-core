package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.converter.ItemSpecConverter
import ru.scisolutions.scicmscore.persistence.converter.LinkedHashSetStringConverter

@Entity
@Table(name = "core_items")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
@org.hibernate.annotations.NaturalIdCache
class Item(
    @Column(nullable = false, unique = true)
    @org.hibernate.annotations.NaturalId
    var name: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String = name,

    @Column(name = "plural_name", nullable = false)
    var pluralName: String,

    @Column(name = "display_plural_name", nullable = false)
    var displayPluralName: String = pluralName,

    @Column(name = "datasource_id")
    var datasourceId: String? = null,

    @ManyToOne
    @JoinColumn(name = "datasource_id", insertable = false, updatable = false)
    var datasource: Datasource? = null,

    @Column(name = "table_name")
    var tableName: String? = null,

    @Column(name = "query")
    var query: String? = null,

    @Column(name = "title_attribute", nullable = false)
    var titleAttribute: String = ID_ATTR_NAME,

    @Column(name = "include_templates")
    @Convert(converter = LinkedHashSetStringConverter::class)
    var includeTemplates: LinkedHashSet<String> = LinkedHashSet(listOf(ItemTemplate.DEFAULT_ITEM_TEMPLATE_NAME)),

    var description: String? = null,

    @Column(name = "read_only", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var readOnly: Boolean = false,

    var icon: String? = null,

    @Column(name = "core", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var core: Boolean = false,

    @Column(name = "perform_ddl", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var performDdl: Boolean = true,

    @Column(name = "versioned", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var versioned: Boolean = false,

    @Column(name = "manual_versioning", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var manualVersioning: Boolean = false,

    @Column(name = "revision_policy_id")
    var revisionPolicyId: String? = null,

    @Column(name = "not_lockable", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var notLockable: Boolean = false,

    @Column(name = "localized", columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var localized: Boolean = false,

    @Column(name = "implementation")
    var implementation: String? = null,

    @Convert(converter = ItemSpecConverter::class)
    var spec: ItemSpec = ItemSpec(),

    var checksum: String? = null,
    var hash: String? = null,

    // @ManyToMany(cascade = [CascadeType.ALL])
    // @JoinTable(
    //     name = "sec_allowed_permissions",
    //     joinColumns = [JoinColumn(name = "source_id")],
    //     inverseJoinColumns = [JoinColumn(name = "target_id")],
    // )
    // val allowedPermissions: MutableSet<Permission> = mutableSetOf()
) : AbstractEntity() {
    override fun toString(): String = "Item(name=$name)"

    val ds: String
        get() = datasource?.name ?: Datasource.MAIN_DATASOURCE_NAME

    val qs: String
        get() {
            val t = if (tableName.isNullOrBlank()) null else tableName
            val q = if (query.isNullOrBlank()) null else "($query)"

            return t ?: q ?: throw IllegalStateException("Table name anq query are empty")
        }

    companion object {
        const val ACCESS_ITEM_NAME = "access"
        const val ALLOWED_PERMISSION_ITEM_NAME = "allowedPermission"
        const val DASHBOARD_ITEM_NAME = "dashboard"
        const val DATASET_ITEM_NAME = "dataset"
        const val EXAMPLE_ITEM_NAME = "example"
        const val GROUP_ITEM_NAME = "group"
        const val GROUP_MEMBER_ITEM_NAME = "groupMember"
        const val GROUP_ROLE_ITEM_NAME = "groupRole"
        const val IDENTITY_ITEM_NAME = "identity"
        const val ITEM_TEMPLATE_ITEM_NAME = "itemTemplate"
        const val ITEM_ITEM_NAME = "item"
        const val LIFECYCLE_ITEM_NAME = "lifecycle"
        const val MEDIA_ITEM_NAME = "media"
        const val PERMISSION_ITEM_NAME = "permission"
        const val REVISION_POLICY_ITEM_NAME = "revisionPolicy"
        const val ROLE_ITEM_NAME = "role"
        const val USER_ITEM_NAME = "user"

        private const val ID_ATTR_NAME = "id"
    }
}
