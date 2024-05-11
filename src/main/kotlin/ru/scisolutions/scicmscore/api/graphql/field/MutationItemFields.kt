package ru.scisolutions.scicmscore.api.graphql.field

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.NonNullType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.extension.upperFirst

@Component
class MutationItemFields {
    fun create(item: Item): FieldDefinition {
        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(DATA_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )

        if (item.versioned && item.manualVersioning)
            builder.inputValueDefinition(InputValues.NON_NULL_MAJOR_REV)

        if (item.localized)
            builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun createVersion(item: Item): FieldDefinition {
        if (!item.versioned)
            throw IllegalStateException("Item [${item.name}] is not versioned. CreateVersion mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}Version")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(DATA_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("copyCollectionRelations")
                    .type(TypeNames.BOOLEAN)
                    .build()
            )

        if (item.manualVersioning)
            builder.inputValueDefinition(InputValues.NON_NULL_MAJOR_REV)

        if (item.localized)
            builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun createLocalization(item: Item): FieldDefinition {
        if (!item.localized)
            throw IllegalStateException("Item [${item.name}] is not localized. CreateLocalization mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("create${capitalizedItemName}Localization")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(DATA_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("copyCollectionRelations")
                    .type(TypeNames.BOOLEAN)
                    .build()
            )

        builder.inputValueDefinition(InputValues.NON_NULL_LOCALE)

        return builder.build()
    }

    fun update(item: Item): FieldDefinition {
        if (item.versioned)
            throw IllegalStateException("Item [${item.name}] is versioned. Update mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("update${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(DATA_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeName("${capitalizedItemName}Input")))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun delete(item: Item): FieldDefinition {
        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("delete${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("deletingStrategy")
                    .type(NonNullType(TypeNames.DELETING_STRATEGY))
                    .build()
            )

        // if (item.versioned)
        //     builder.inputValueDefinition(InputValues.MAJOR_REV)
        //
        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun purge(item: Item): FieldDefinition {
        if (!item.versioned)
            throw IllegalStateException("Item [${item.name}] is not versioned. Purge mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("purge${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("deletingStrategy")
                    .type(NonNullType(TypeNames.DELETING_STRATEGY))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun lock(item: Item): FieldDefinition {
        if (item.notLockable)
            throw IllegalArgumentException("Item [${item.name}] is not lockable. Lock mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("lock${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}FlaggedResponse"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun unlock(item: Item): FieldDefinition {
        if (item.notLockable)
            throw IllegalArgumentException("Item [${item.name}] is not lockable. Unlock mutation cannot be applied.")

        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("unlock${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}FlaggedResponse"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun promote(item: Item): FieldDefinition {
        val capitalizedItemName = item.name.upperFirst()
        val builder = FieldDefinition.newFieldDefinition()
            .name("promote${capitalizedItemName}")
            .type(TypeName("${capitalizedItemName}Response"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name(ID_INPUT_VALUE_NAME)
                    .type(NonNullType(TypeNames.ID))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("state")
                    .type(NonNullType(TypeNames.STRING))
                    .build()
            )

        // if (item.localized)
        //     builder.inputValueDefinition(InputValues.LOCALE)

        return builder.build()
    }

    fun customMethods(item: Item, customMethodNames: Set<String>): List<FieldDefinition> {
        val fields = mutableListOf<FieldDefinition>()
        val capitalizedItemName = item.name.upperFirst()
        for (methodName in customMethodNames) {
            fields.add(
                FieldDefinition.newFieldDefinition()
                    .name("${methodName}${capitalizedItemName}")
                    .type(TypeName("${capitalizedItemName}CustomMethodResponse"))
                    .inputValueDefinition(
                        InputValueDefinition.newInputValueDefinition()
                            .name(DATA_INPUT_VALUE_NAME)
                            .type(TypeNames.OBJECT)
                            .build()
                    )
                    .build()
            )
        }

        return fields.toList()
    }

    companion object {
        private const val ID_INPUT_VALUE_NAME = "id"
        private const val DATA_INPUT_VALUE_NAME = "data"
    }
}