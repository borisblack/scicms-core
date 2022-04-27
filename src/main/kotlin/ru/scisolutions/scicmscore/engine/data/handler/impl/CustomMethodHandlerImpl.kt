package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse
import ru.scisolutions.scicmscore.service.ItemService
import java.lang.reflect.Modifier

@Service
class CustomMethodHandlerImpl(
    private val itemService: ItemService,
    private val applicationContext: ApplicationContext
) : CustomMethodHandler {
    override fun getCustomMethods(itemName: String): Set<String> {
        val item = itemService.items[itemName] ?: throw IllegalArgumentException("Item [$itemName] not found")
        val implementation = item.implementation ?: throw IllegalStateException("Item [$itemName] has no implementation")

        return implementation.declaredMethods.asSequence()
            .filter { Modifier.isPublic(it.modifiers) }
            .filter {
                if (it.parameterCount != 1 || it.parameterTypes[0] != CustomMethodInput::class.java || it.returnType != CustomMethodResponse::class.java) {
                    logger.info("Method [{}#{}] has invalid signature. Skipping this method", implementation.simpleName, it.name)
                    false
                } else
                    true
            }
            .filter {
                if (it.name in reservedMethodNames) {
                    logger.info("Method [{}#{}] name is reserved. Skipping this method", implementation.simpleName, it.name)
                    false
                } else
                    true
            }
            .map { it.name }
            .toSet()
    }

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse {
        val item = itemService.items[itemName] ?: throw IllegalArgumentException("Item [$itemName] not found")
        val implementation = item.implementation ?: throw IllegalStateException("Item [$itemName] has no implementation")
        val customMethod = implementation.getMethod(methodName, CustomMethodInput::class.java)
            ?: throw IllegalStateException("Method [$methodName] with valid signature not found")

        if (customMethod.returnType != CustomMethodResponse::class.java)
            throw IllegalArgumentException("Method [${implementation.simpleName}#${customMethod.name}] has invalid signature")

        val instance = getInstance(implementation)
        return customMethod.invoke(instance, customMethodInput) as CustomMethodResponse
    }

    private fun getInstance(clazz: Class<*>) =
        try {
            applicationContext.getBean(clazz)
        } catch(e: BeansException) {
            clazz.getConstructor().newInstance()
        }

    companion object {
        private const val CREATE_METHOD_NAME = "create"
        private const val UPDATE_METHOD_NAME = "update"
        private const val DELETE_METHOD_NAME = "delete"
        private const val PURGE_METHOD_NAME = "purge"
        private const val LOCK_METHOD_NAME = "lock"
        private const val UNLOCK_METHOD_NAME = "unlock"
        private const val PROMOTE_METHOD_NAME = "promote"

        private val reservedMethodNames = setOf(
            CREATE_METHOD_NAME,
            UPDATE_METHOD_NAME,
            DELETE_METHOD_NAME,
            PURGE_METHOD_NAME,
            LOCK_METHOD_NAME,
            UNLOCK_METHOD_NAME,
            PROMOTE_METHOD_NAME
        )

        private val logger = LoggerFactory.getLogger(CustomMethodHandlerImpl::class.java)
    }
}