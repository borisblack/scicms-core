package ru.scisolutions.scicmscore.persistence.entity

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

    @Convert(converter = ItemSpecConverter::class)
    var spec: ItemSpec = ItemSpec(),

    var checksum: String? = null,
    var hash: String? = null
) : AbstractEntity() {
    override fun toString(): String = "ItemTemplate(name=$name)"
}
