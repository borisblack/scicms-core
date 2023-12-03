package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.converter.ItemSpecConverter

@Entity
@Table(name = "core_item_templates")
class ItemTemplate(
    @Column(nullable = false)
    var name: String,

    @Column(name = "plural_name", nullable = false)
    var pluralName: String,

    @Column(columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var core: Boolean = false,

    @Convert(converter = ItemSpecConverter::class)
    var spec: ItemSpec = ItemSpec(),

    var checksum: String? = null,
    var hash: String? = null
) : AbstractEntity() {
    override fun toString(): String = "ItemTemplate(name=$name)"

    companion object {
        const val DEFAULT_ITEM_TEMPLATE_NAME = "default"
    }
}
