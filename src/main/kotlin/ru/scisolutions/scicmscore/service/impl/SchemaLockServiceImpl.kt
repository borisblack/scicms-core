package ru.scisolutions.scicmscore.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.persistence.repository.SchemaLockRepository
import ru.scisolutions.scicmscore.service.SchemaLockService
import java.net.InetAddress
import java.time.LocalDateTime

@Service
@Repository
@Transactional
class SchemaLockServiceImpl(
    private val schemaProps: SchemaProps,
    private val schemaLockRepository: SchemaLockRepository
) : SchemaLockService {
    private val hostName = InetAddress.getLocalHost().hostName

    override fun lock(): Boolean {
        val lockResult = schemaLockRepository.lock(hostName, LocalDateTime.now().plusSeconds(schemaProps.itemLockLockDurationSeconds))

        return if (lockResult == 1) {
            logger.debug("Successfully acquired SchemaLock lock")
            true
        } else {
            logger.warn(LOCK_FAIL_MSG)
            false
        }
    }

    override fun lockOrThrow() {
        if (!lock())
            throw IllegalStateException(LOCK_FAIL_MSG)
    }

    override fun unlock(): Boolean {
        val result = schemaLockRepository.unlock(hostName)

        return if (result == 1) {
            logger.debug("Successfully released SchemaLock lock")
            true
        } else {
            logger.warn(UNLOCK_FAIL_MSG)
            false
        }
    }

    override fun unlockOrThrow() {
        if (!unlock())
            throw IllegalStateException(UNLOCK_FAIL_MSG)
    }

    companion object {
        private const val LOCK_FAIL_MSG = "Cannot acquire SchemaLock lock. It was locked by another instance"
        private const val UNLOCK_FAIL_MSG = "Cannot release SchemaLock lock"

        private val logger = LoggerFactory.getLogger(SchemaLockServiceImpl::class.java)
    }
}