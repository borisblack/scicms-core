package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.converter.ItemSpecConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_item_templates")
class ItemTemplate(
    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
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
