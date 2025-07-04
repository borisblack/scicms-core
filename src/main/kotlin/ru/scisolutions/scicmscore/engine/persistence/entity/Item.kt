package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.engine.model.ItemSpec
import ru.scisolutions.scicmscore.engine.persistence.converter.ItemSpecConverter
import ru.scisolutions.scicmscore.engine.persistence.converter.LinkedHashSetStringConverter

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
    @Column(name = "cache_ttl")
    var cacheTtl: Int? = null,
    @Column(name = "id_attribute", nullable = false)
    var idAttribute: String = ID_ATTR_NAME,
    @Column(name = "title_attribute", nullable = false)
    var titleAttribute: String = ID_ATTR_NAME,
    @Column(name = "default_sort_attribute")
    var defaultSortAttribute: String? = null,
    @Column(name = "default_sort_order")
    var defaultSortOrder: String? = null,
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
    var hash: String? = null
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

    val idColName: String
        get() = spec.getAttribute(idAttribute).getColumnName(idAttribute)

    fun hasAttribute(attrName: String) = attrName in spec.attributes

    fun hasIdAttribute() = hasAttribute(ID_ATTR_NAME)

    fun hasConfigIdAttribute() = hasAttribute(CONFIG_ID_ATTR_NAME)

    fun hasGenerationAttribute() = hasAttribute(GENERATION_ATTR_NAME)

    fun hasMajorRevAttribute() = hasAttribute(MAJOR_REV_ATTR_NAME)

    fun hasMinorRevAttribute() = hasAttribute(MINOR_REV_ATTR_NAME)

    fun hasCurrentAttribute() = hasAttribute(CURRENT_ATTR_NAME)

    fun hasLocaleAttribute() = hasAttribute(LOCALE_ATTR_NAME)

    fun hasLifecycleAttribute() = hasAttribute(LIFECYCLE_ATTR_NAME)

    fun hasStateAttribute() = hasAttribute(STATE_ATTR_NAME)

    fun hasPermissionAttribute() = hasAttribute(PERMISSION_ATTR_NAME)

    fun hasCreatedAtAttribute() = hasAttribute(CREATED_AT_ATTR_NAME)

    fun hasCreatedByAttribute() = hasAttribute(CREATED_BY_ATTR_NAME)

    fun hasUpdatedAtAttribute() = hasAttribute(UPDATED_AT_ATTR_NAME)

    fun hasUpdatedByAttribute() = hasAttribute(UPDATED_BY_ATTR_NAME)

    fun hasLockedByAttribute() = hasAttribute(LOCKED_BY_ATTR_NAME)

    companion object {
        const val ACCESS_ITEM_NAME = "access"
        const val ALLOWED_PERMISSION_ITEM_NAME = "allowedPermission"
        const val DASHBOARD_ITEM_NAME = "dashboard"
        const val DASHBOARD_CATEGORY_ITEM_NAME = "dashboardCategory"
        const val DASHBOARD_CATEGORY_HIERARCHY_ITEM_NAME = "dashboardCategoryHierarchy"
        const val DASHBOARD_CATEGORY_MAP_ITEM_NAME = "dashboardCategoryMap"
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
        private const val CONFIG_ID_ATTR_NAME = "configId"
        private const val GENERATION_ATTR_NAME = "generation"
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val MINOR_REV_ATTR_NAME = "minorRev"
        private const val CURRENT_ATTR_NAME = "current"
        private const val LOCALE_ATTR_NAME = "locale"
        const val LIFECYCLE_ATTR_NAME = "lifecycle"
        private const val STATE_ATTR_NAME = "state"
        const val PERMISSION_ATTR_NAME = "permission"
        private const val CREATED_AT_ATTR_NAME = "createdAt"
        const val CREATED_BY_ATTR_NAME = "createdBy"
        private const val UPDATED_AT_ATTR_NAME = "updatedAt"
        const val UPDATED_BY_ATTR_NAME = "updatedBy"
        const val LOCKED_BY_ATTR_NAME = "lockedBy"
    }
}
