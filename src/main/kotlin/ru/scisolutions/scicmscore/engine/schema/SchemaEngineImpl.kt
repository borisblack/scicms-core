package ru.scisolutions.scicmscore.engine.schema

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.handler.relation.ManyToManyRelationHandler
import ru.scisolutions.scicmscore.engine.schema.handler.relation.ManyToOneRelationHandler
import ru.scisolutions.scicmscore.engine.schema.handler.relation.OneToManyRelationHandler
import ru.scisolutions.scicmscore.engine.schema.handler.relation.OneToOneRelationHandler
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.engine.schema.model.relation.Relation
import ru.scisolutions.scicmscore.engine.schema.reader.ModelsReader
import ru.scisolutions.scicmscore.engine.schema.seeder.SchemaSeeder
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class SchemaEngineImpl(
    schemaProps: SchemaProps,
    modelsReader: ModelsReader,
    schemaSeeder: SchemaSeeder,
    private val oneToOneRelationHandler: OneToOneRelationHandler,
    private val manyToOneRelationHandler: ManyToOneRelationHandler,
    private val oneToManyRelationHandler: OneToManyRelationHandler,
    private val manyToManyRelationHandler: ManyToManyRelationHandler
) : SchemaEngine {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    init {
        if (schemaProps.readOnInit) {
            logger.info("Schema read flag enabled. Trying to read")
            val models = modelsReader.read()
            addModels(models)
        }

        if (schemaProps.seedOnInit) {
            logger.info("Schema seed flag enabled. Trying to seed")
            schemaSeeder.seed(getItems())
        }
    }

    final override fun addModel(model: AbstractModel) {
        val metadata = model.metadata
        if(metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${metadata.name}] must start with a lowercase character")

        when(model) {
            is ItemTemplate -> {
                logger.info("Validating item template [{}]", metadata.name)
                itemSpecValidator.validate(model.spec)
                itemTemplates[metadata.name] = model
            }
            is Item -> {
                logger.info("Validating item [{}]", metadata.name)
                itemSpecValidator.validate(model.spec)
                items[metadata.name] = model
            }
            else -> throw IllegalArgumentException("${model::class.simpleName} model type is not supported")
        }
    }

    final override fun addModels(models: Collection<AbstractModel>) {
        for (model in models) {
            addModel(model)
        }
    }

    final override fun getItem(name: String): Item {
        val itemModel = items[name] ?: throw IllegalArgumentException("Item $name not found")
        return includeTemplates(itemModel)
    }

    final override fun getItems(): Map<String, Item> =
        items.mapValues { (_, value) -> includeTemplates(value) }

    private fun includeTemplates(item: Item): Item {
        var mergedItem = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = itemTemplates[templateName] ?: throw IllegalStateException("Template $templateName not found")
            mergedItem = mergedItem.includeTemplate(itemTemplate)
        }
        return mergedItem
    }

    override fun getAttributeRelation(item: ItemEntity, attrName: String): Relation {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (attribute.type != Type.relation)
            throw IllegalArgumentException("Attribute [$attrName] must be of relation type")

        requireNotNull(attribute.relType) { "The [$attrName] attribute does not have a relType field" }

        return when (attribute.relType) {
            RelType.oneToOne -> oneToOneRelationHandler.getAttributeRelation(item, attrName)
            RelType.manyToOne -> manyToOneRelationHandler.getAttributeRelation(item, attrName)
            RelType.oneToMany -> oneToManyRelationHandler.getAttributeRelation(item, attrName)
            RelType.manyToMany -> manyToManyRelationHandler.getAttributeRelation(item, attrName)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaEngineImpl::class.java)
        private val itemSpecValidator = ItemSpecValidator()
    }
}