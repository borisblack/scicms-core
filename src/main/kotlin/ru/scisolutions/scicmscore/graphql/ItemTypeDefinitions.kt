package ru.scisolutions.scicmscore.graphql

import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.entity.Item
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ItemTypeDefinitions {
    fun getObjectType(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        item.spec.attributes
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                val type = typeResolver.objectType(attrName, attribute)
                typeBuilder.fieldDefinition(
                    FieldDefinition.newFieldDefinition()
                        .name(attrName)
                        .type(type)
                        .description(Description(attribute.description, null, false))
                        .build()
                )
            }

        return typeBuilder.build()
    }

    fun responseObjectType(item: Item): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}Response")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(TypeName(item.name.capitalize()))
                    .build()
            )
            .build()

    fun responseCollectionObjectType(item: Item): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}ResponseCollection")
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("data")
                    .type(
                        NonNullType(
                            ListType(
                                NonNullType(
                                    TypeName(item.name.capitalize())
                                )
                            )
                        )
                    )
                    .build()
            )
            .fieldDefinition(
                FieldDefinition.newFieldDefinition()
                    .name("meta")
                    .type(
                        NonNullType(
                            TypeName("ResponseCollectionMeta")
                        )
                    )
                    .build()
            )
            .build()

    fun filtersInputObjectType(item: Item): InputObjectTypeDefinition {
        val inputName = "${item.name.capitalize()}FiltersInput"
        val inputBuilder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name(inputName)

        item.spec.attributes
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromFiltersInputObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                val type = typeResolver.filterInputType(attrName, attribute)
                inputBuilder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(type)
                        .build()
                )
            }

        inputBuilder
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("and")
                    .type(ListType(TypeName(inputName)))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("or")
                    .type(ListType(TypeName(inputName)))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("not")
                    .type(TypeName(inputName))
                    .build()
            )

        return inputBuilder.build()
    }

    fun inputObjectType(item: Item): InputObjectTypeDefinition {
        val inputBuilder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        item.spec.attributes
            .filter { (attrName, attribute) -> excludeAttributePolicy.excludeFromInputObjectType(item, attrName, attribute) }
            .forEach { (attrName, attribute) ->
                val type = typeResolver.inputType(attrName, attribute)
                inputBuilder.inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name(attrName)
                        .type(type)
                        .build()
                )
            }

        return inputBuilder.build()
    }

    fun responseQueryField(item: Item): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.capitalize()}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.versioned)
        //     builder.inputValueDefinition(majorRevInput())
        //
        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    private fun localeInput(): InputValueDefinition =
        InputValueDefinition.newInputValueDefinition()
            .name(LOCALE_ATTR_NAME)
            .type(TypeName("String"))
            .build()

    private fun majorRevInput(required: Boolean = false): InputValueDefinition {
        val stringType = TypeName("String")
        return InputValueDefinition.newInputValueDefinition()
            .name(MAJOR_REV_ATTR_NAME)
            .type(if (required) NonNullType(stringType) else stringType)
            .build()
    }

    fun responseCollectionQueryField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.pluralName)
            .type(TypeName("${name}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("filters")
                    .type(TypeName("${name}FiltersInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("pagination")
                    .type(TypeName("PaginationInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("sort")
                    .type(ListType(TypeName("String")))
                    .build()
            )

        if (item.versioned)
            builder.inputValueDefinition(majorRevInput())

        if (item.localized)
            builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun createMutationField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${name}Input")))
                    .build()
            )

        if (item.versioned && item.manualVersioning)
            builder.inputValueDefinition(majorRevInput(true))

        if (item.localized)
            builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun createVersionMutationField(item: Item): FieldDefinition {
        if (!item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is not versioned. CreateVersion mutation cannot be applied")

        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${name}Version")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${name}Input")))
                    .build()
            )

        if (item.manualVersioning)
            builder.inputValueDefinition(majorRevInput(true))

        if (item.localized)
            builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun createLocalizationMutationField(item: Item): FieldDefinition {
        if (!item.localized)
            throw IllegalArgumentException("Item [${item.name}] is not localized. CreateLocalization mutation cannot be applied")

        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${name}Localization")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${name}Input")))
                    .build()
            )

        builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun updateMutationField(item: Item): FieldDefinition {
        if (item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is versioned. Update mutation cannot be applied")

        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("update${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(NonNullType(TypeName("${name}Input")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun deleteMutationField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("delete${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.versioned)
        //     builder.inputValueDefinition(majorRevInput())
        //
        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun purgeMutationField(item: Item): FieldDefinition {
        if (!item.versioned)
            throw IllegalArgumentException("Item [${item.name}] is not versioned. Purge mutation cannot be applied")

        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("purge${name}")
            .type(TypeName("${name}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun lockMutationField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("lock${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun unlockMutationField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("unlock${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun promoteMutationField(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("promote${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("state")
                    .type(NonNullType(TypeName("String")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(localeInput())

        return builder.build()
    }

    fun listCustomMutationFields(item: Item): List<FieldDefinition> {
        if (item.implementation.isNullOrBlank())
            throw IllegalArgumentException("Item [${item.name}] has no implementation")

        val list = mutableListOf<FieldDefinition>()
        val clazz = Class.forName(item.implementation)
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
            .forEach { list.add(customMutationField(item, it)) }

        return list
    }

    private fun customMutationField(item: Item, method: Method): FieldDefinition =
        FieldDefinition.newFieldDefinition()
            .name("${method.name}${item.name.capitalize()}")
            .type(TypeName("JSON"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("data")
                    .type(TypeName("JSON"))
                    .build()
            )
            .build()

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val LOCALE_ATTR_NAME = "locale"
        private const val CREATE_METHOD_NAME = "create"
        private const val UPDATE_METHOD_NAME = "update"
        private const val DELETE_METHOD_NAME = "delete"
        private const val PURGE_METHOD_NAME = "purge"
        private const val LOCK_METHOD_NAME = "lock"
        private const val UNLOCK_METHOD_NAME = "unlock"
        private const val PROMOTE_METHOD_NAME = "promote"
        private val reservedMethodNames = setOf(CREATE_METHOD_NAME, UPDATE_METHOD_NAME, DELETE_METHOD_NAME, PURGE_METHOD_NAME, LOCK_METHOD_NAME, UNLOCK_METHOD_NAME, PROMOTE_METHOD_NAME)
        private val logger = LoggerFactory.getLogger(ItemTypeDefinitions::class.java)
        private val typeResolver = TypeResolver()
        private val excludeAttributePolicy = ExcludeAttributePolicy()
    }
}