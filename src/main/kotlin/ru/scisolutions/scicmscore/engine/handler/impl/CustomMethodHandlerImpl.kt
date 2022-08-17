package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.model.CreateHook
import ru.scisolutions.scicmscore.engine.model.CreateLocalizationHook
import ru.scisolutions.scicmscore.engine.model.CreateVersionHook
import ru.scisolutions.scicmscore.engine.model.DeleteHook
import ru.scisolutions.scicmscore.engine.model.FindAllHook
import ru.scisolutions.scicmscore.engine.model.FindOneHook
import ru.scisolutions.scicmscore.engine.model.LockHook
import ru.scisolutions.scicmscore.engine.model.PurgeHook
import ru.scisolutions.scicmscore.engine.model.UpdateHook
import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.response.CustomMethodResponse
import ru.scisolutions.scicmscore.persistence.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.lang.reflect.Modifier

@Service
class CustomMethodHandlerImpl(
    private val classService: ClassService,
    private val itemService: ItemService
) : CustomMethodHandler {
    override fun getCustomMethods(itemName: String): Set<String> {
        val item = itemService.getByName(itemName)
        val implementation = item.implementation
        if (implementation.isNullOrBlank())
            throw IllegalArgumentException("Item [$itemName] has no implementation.")

        val clazz = classService.getClass(implementation)

        return clazz.declaredMethods.asSequence()
            .filter { Modifier.isPublic(it.modifiers) }
            .filter {
                if (it.parameterCount != 1 || it.parameterTypes[0] != CustomMethodInput::class.java || it.returnType != CustomMethodResponse::class.java) {
                    logger.info("Method [{}#{}] has invalid signature. Skipping this method", clazz.simpleName, it.name)
                    false
                } else
                    true
            }
            .filter {
                if (it.name in reservedMethodNames) {
                    logger.info("Method [{}#{}] name is reserved. Skipping this method", clazz.simpleName, it.name)
                    false
                } else
                    true
            }
            .map { it.name }
            .toSet()
    }

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse {
        val item = itemService.getByName(itemName)
        if (itemService.findByNameForWrite(item.name) == null)
            throw AccessDeniedException("You are not allowed to call custom method.")

        val implementation = item.implementation
        if (implementation.isNullOrBlank())
            throw IllegalStateException("Item [$itemName] has no implementation.")

        val clazz = classService.getClass(implementation)
        val customMethod = clazz.getMethod(methodName, CustomMethodInput::class.java)
            ?: throw IllegalStateException("Method [$methodName] with valid signature not found.")

        if (customMethod.returnType != CustomMethodResponse::class.java)
            throw IllegalArgumentException("Method [${clazz.simpleName}#${customMethod.name}] has invalid signature.")

        val instance = classService.getInstance(clazz)
        return customMethod.invoke(instance, customMethodInput) as CustomMethodResponse
    }

    companion object {
        private const val CREATE_METHOD_NAME = "create"
        private const val CREATE_VERSION_METHOD_NAME = "createVersion"
        private const val CREATE_LOCALIZATION_METHOD_NAME = "createLocalization"
        private const val UPDATE_METHOD_NAME = "update"
        private const val DELETE_METHOD_NAME = "delete"
        private const val PURGE_METHOD_NAME = "purge"
        private const val LOCK_METHOD_NAME = "lock"
        private const val UNLOCK_METHOD_NAME = "unlock"
        private const val PROMOTE_METHOD_NAME = "promote"

        private val reservedMethodNames = setOf(
            CREATE_METHOD_NAME,
            CREATE_VERSION_METHOD_NAME,
            CREATE_LOCALIZATION_METHOD_NAME,
            UPDATE_METHOD_NAME,
            DELETE_METHOD_NAME,
            PURGE_METHOD_NAME,
            LOCK_METHOD_NAME,
            UNLOCK_METHOD_NAME,
            PROMOTE_METHOD_NAME,
            FindOneHook::beforeFindOne.name,
            FindOneHook::afterFindOne.name,
            FindAllHook::beforeFindAll.name,
            FindAllHook::afterFindAll.name,
            CreateHook::beforeCreate.name,
            CreateHook::afterCreate.name,
            CreateVersionHook::beforeCreateVersion.name,
            CreateVersionHook::afterCreateVersion.name,
            CreateLocalizationHook::beforeCreateLocalization.name,
            CreateLocalizationHook::afterCreateLocalization.name,
            UpdateHook::beforeUpdate.name,
            UpdateHook::afterUpdate.name,
            DeleteHook::beforeDelete.name,
            DeleteHook::afterDelete.name,
            PurgeHook::beforePurge.name,
            PurgeHook::afterPurge.name,
            LockHook::beforeLock.name,
            LockHook::afterLock.name,
            LockHook::beforeUnlock.name,
            LockHook::afterUnlock.name
        )

        private val logger = LoggerFactory.getLogger(CustomMethodHandlerImpl::class.java)
    }
}