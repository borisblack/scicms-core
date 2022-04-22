package ru.scisolutions.scicmscore.graphql.field.builder.mutation

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.graphql.TypeNames
import ru.scisolutions.scicmscore.graphql.field.builder.FieldDefinitionListBuilder
import java.lang.reflect.Modifier

class ImplementationFieldListBuilder(private val item: Item) : FieldDefinitionListBuilder {
    override fun buildList(): List<FieldDefinition> {
        if (item.implementation.isNullOrBlank())
            throw IllegalArgumentException("Item [${item.name}] has no implementation")

        val fields = mutableListOf<FieldDefinition>()
        val clazz = Class.forName(item.implementation)
        val capitalizedItemName = item.name.capitalize()
        clazz.declaredMethods.asSequence()
            .filter { Modifier.isPublic(it.modifiers) }
            .filter {
                if (it.parameterCount != 1 || !it.parameterTypes[0].isAssignableFrom(DataFetchingEnvironment::class.java)) {
                    logger.info("Method [{}#{}] has invalid signature. Skipping this method", clazz.simpleName, it.name)
                    false
                } else
                    true
            }
            // .filter { it.returnType != Unit::class.java }
            .filter {
                if (it.name in reservedMethodNames) {
                    logger.info("Method [{}#{}] name is reserved. Skipping this method", clazz.simpleName, it.name)
                    false
                } else
                    true
            }
            .forEach {
                val fieldName = "${it.name}${capitalizedItemName}"
                fields.add(newJsonField(fieldName))
            }

        return fields.toList()
    }

    private fun newJsonField(fieldName: String) =
        FieldDefinition.newFieldDefinition()
            .name(fieldName)
            .type(TypeNames.JSON)
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(TypeNames.JSON)
                    .build()
            )
            .build()

    companion object {
        private const val CREATE_METHOD_NAME = "create"
        private const val UPDATE_METHOD_NAME = "update"
        private const val DELETE_METHOD_NAME = "delete"
        private const val PURGE_METHOD_NAME = "purge"
        private const val LOCK_METHOD_NAME = "lock"
        private const val UNLOCK_METHOD_NAME = "unlock"
        private const val PROMOTE_METHOD_NAME = "promote"
        private val reservedMethodNames = setOf(CREATE_METHOD_NAME, UPDATE_METHOD_NAME, DELETE_METHOD_NAME, PURGE_METHOD_NAME, LOCK_METHOD_NAME, UNLOCK_METHOD_NAME, PROMOTE_METHOD_NAME)
        private val logger = LoggerFactory.getLogger(ImplementationFieldListBuilder::class.java)
    }
}