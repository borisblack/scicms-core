package ru.scisolutions.scicmscore.graphql

import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ru.scisolutions.scicmscore.entity.Item

class ItemTypeDefinitions {
    fun getTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        for ((name, attribute) in item.spec.attributes) {
            val type = typeResolver.objectType(name, attribute)
            val fieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
                .name(name)
                .type(type)
                .description(Description(attribute.description, null, false))

            typeBuilder.fieldDefinition(fieldDefinitionBuilder.build())
        }

        return typeBuilder.build()
    }

    fun getResponseTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}Response")

        val dataFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
            .name("data")
            .type(TypeName(item.name.capitalize()))

        typeBuilder.fieldDefinition(dataFieldDefinitionBuilder.build())

        return typeBuilder.build()
    }

    fun getResponseCollectionTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}ResponseCollection")

        val dataFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
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

        typeBuilder.fieldDefinition(dataFieldDefinitionBuilder.build())

        val metaFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
            .name("meta")
            .type(
                NonNullType(
                    TypeName("ResponseCollectionMeta")
                )
            )

        typeBuilder.fieldDefinition(metaFieldDefinitionBuilder.build())

        return typeBuilder.build()
    }

    fun getFiltersInputTypeDefinition(item: Item): InputObjectTypeDefinition {
        val inputName = "${item.name.capitalize()}FiltersInput"
        val inputBuilder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name(inputName)

        for ((name, attribute) in item.spec.attributes) {
            val type = typeResolver.filterInputType(name, attribute)
            val inputValueDefinitionBuilder = InputValueDefinition.newInputValueDefinition()
                .name(name)
                .type(type)

            inputBuilder.inputValueDefinition(inputValueDefinitionBuilder.build())
        }

        // and
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("and")
                .type(ListType(TypeName(inputName)))
                .build()
        )

        // or
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("or")
                .type(ListType(TypeName(inputName)))
                .build()
        )

        // not
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("not")
                .type(TypeName(inputName))
                .build()
        )

        return inputBuilder.build()
    }

    fun getInputTypeDefinition(item: Item): InputObjectTypeDefinition {
        val inputBuilder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("${item.name.capitalize()}Input")

        for ((name, attribute) in item.spec.attributes) {
            if (attribute.keyed)
                continue

            // Exclude version attributes
            if (!item.manualVersioning && name == MAJOR_REV_ATTR_NAME)
                continue

            if (name in versionAttributes)
                continue

            // Exclude state attribute (promote is used to change state)
            if (name == STATE_ATTR_NAME)
                continue

            val type = typeResolver.inputType(name, attribute)
            val inputValueDefinitionBuilder = InputValueDefinition.newInputValueDefinition()
                .name(name)
                .type(type)

            inputBuilder.inputValueDefinition(inputValueDefinitionBuilder.build())
        }

        return inputBuilder.build()
    }

    fun getResponseQueryDefinition(item: Item): FieldDefinition {
        val builder = FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.capitalize()}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(TypeName("ID"))
                    .build()
            )
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    private fun addLocalizationInputIfRequired(builder: FieldDefinition.Builder, item: Item) {
        if (item.localized)
            builder
                .inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition()
                        .name("locale")
                        .type(TypeName("String"))
                        .build()
                )
    }

    fun getResponseCollectionQueryDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getCreateMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getUpdateMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getDeleteMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getPurgeMutationDefinition(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        val builder = FieldDefinition.newFieldDefinition()
            .name("purge${name}")
            .type(TypeName("${name}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(NonNullType(TypeName("ID")))
                    .build()
            )
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getLockMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getUnlockMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    fun getPromoteMutationDefinition(item: Item): FieldDefinition {
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
        addLocalizationInputIfRequired(builder, item)

        return builder.build()
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val GENERATION_ATTR_NAME = "generation"
        private const val LAST_VERSION_ATTR_NAME = "lastVersion"
        private const val CURRENT_ATTR_NAME = "current"
        private const val STATE_ATTR_NAME = "state"

        private val versionAttributes = setOf(GENERATION_ATTR_NAME, LAST_VERSION_ATTR_NAME, CURRENT_ATTR_NAME)
        private val typeResolver = TypeResolver()
    }
}