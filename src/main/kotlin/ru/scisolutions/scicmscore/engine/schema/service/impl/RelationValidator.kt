package ru.scisolutions.scicmscore.engine.schema.service.impl

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Component
class RelationValidator(
    private val dbSchema: DbSchema,
    private val itemService: ItemService
) {
    fun validateAttribute(item: Item, attrName: String, attribute: Attribute) {
        validateAttribute(attrName, attribute)
        validateDataSource(item, attribute)
    }

    private fun validateAttribute(attrName: String, attribute: Attribute) {
        if (attribute.type != Type.relation)
            throw IllegalArgumentException("Unsupported attribute type")

        requireNotNull(attribute.relType) { "The [$attrName] attribute has a relation type, but relType is null" }
        requireNotNull(attribute.target) { "The [$attrName] attribute has a relation type, but target is null" }

        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        if (attribute.relType == RelType.oneToMany) {
            requireNotNull(attribute.mappedBy) {
                "The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship"
            }
        }

        if (attribute.relType == RelType.manyToMany){
            requireNotNull(attribute.intermediate) {
                "The [$attrName] attribute does not have an intermediate field, which is required for the manyToMany relationship"
            }

            if (attribute.inversedBy == null && attribute.mappedBy == null)
                throw IllegalArgumentException("The [$attrName] attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")
        }
    }

    private fun validateDataSource(item: Item, attribute: Attribute) {
        val targetItem = dbSchema.getItemOrThrow(requireNotNull(attribute.target))
        if (attribute.relType == RelType.manyToMany){
            if (item.metadata.dataSource != targetItem.metadata.dataSource)
                throw IllegalStateException("Item [${item.metadata.name}] and it's manyToMany attribute target item have different data sources")

            val intermediateItem = dbSchema.getItemOrThrow(requireNotNull(attribute.intermediate))
            if (item.metadata.dataSource != intermediateItem.metadata.dataSource)
                throw IllegalStateException("Item [${item.metadata.name}] and it's manyToMany attribute intermediate item have different data sources")
        }
    }

    fun validateAttribute(itemEntity: ItemEntity, attrName: String, attribute: Attribute) {
        validateAttribute(attrName, attribute)
        validateDataSource(itemEntity, attribute)
    }

    private fun validateDataSource(itemEntity: ItemEntity, attribute: Attribute) {
        val targetItem = itemService.getByName(requireNotNull(attribute.target))
        if (attribute.relType == RelType.manyToMany){
            if (itemEntity.dataSource != targetItem.dataSource)
                throw IllegalStateException("Item [${itemEntity.name}] and it's manyToMany attribute target item have different data sources")

            val intermediateItem = itemService.getByName(requireNotNull(attribute.intermediate))
            if (itemEntity.dataSource != intermediateItem.dataSource)
                throw IllegalStateException("Item [${itemEntity.name}] and it's manyToMany attribute intermediate item have different data sources")
        }
    }
}