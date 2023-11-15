package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.converter.ItemSpecConverter
import ru.scisolutions.scicmscore.persistence.converter.LinkedHashSetConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_items")
class Item(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String = name,

    @Column(name = "plural_name", nullable = false)
    var pluralName: String,

    @Column(name = "display_plural_name", nullable = false)
    var displayPluralName: String = pluralName,

    @Column(name = "data_source", nullable = false)
    var dataSource: String,

    @Column(name = "table_name")
    var tableName: String? = null,

    @Column(name = "query")
    var query: String? = null,

    @Column(name = "title_attribute", nullable = false)
    var titleAttribute: String = ID_ATTR_NAME,

    @Convert(converter = LinkedHashSetConverter::class)
    var includeTemplates: Set<String> = LinkedHashSet(listOf(ItemTemplate.DEFAULT_ITEM_TEMPLATE_NAME)),

    var description: String? = null,

    @Column(name = "read_only", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var readOnly: Boolean = false,

    var icon: String? = null,

    @Column(name = "core", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var core: Boolean = false,

    @Column(name = "perform_ddl", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var performDdl: Boolean = true,

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

    @Column(name = "page_size")
    var pageSize: Int? = null,

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

    fun getQueryOrThrow(): String {
        val q = if (query == null) null else "($query)"
        return tableName ?: q ?: throw IllegalStateException("Table name anq query are empty")
    }

    companion object {
        const val DASHBOARD_ITEM_NAME = "dashboard"
        const val DATASET_ITEM_NAME = "dataset"
        const val EXAMPLE_ITEM_NAME = "example"
        const val ITEM_TEMPLATE_ITEM_NAME = "itemTemplate"
        const val ITEM_ITEM_NAME = "item"
        const val LIFECYCLE_ITEM_NAME = "lifecycle"
        const val MEDIA_ITEM_NAME = "media"
        const val REVISION_POLICY_ITEM_NAME = "revisionPolicy"

        const val ACCESS_ITEM_NAME = "access"
        const val ALLOWED_PERMISSION_ITEM_NAME = "allowedPermission"
        const val GROUP_ITEM_NAME = "group"
        const val GROUP_MEMBER_ITEM_NAME = "groupMember"
        const val GROUP_ROLE_ITEM_NAME = "groupRole"
        const val IDENTITY_ITEM_NAME = "identity"
        const val PERMISSION_ITEM_NAME = "permission"
        const val ROLE_ITEM_NAME = "role"
        const val USER_ITEM_NAME = "user"

        private const val ID_ATTR_NAME = "id"

        private val whitespaceRegex = "\\s+".toRegex()
    }
}
