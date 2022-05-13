package ru.scisolutions.scicmscore.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.repository.ItemLockRepository
import ru.scisolutions.scicmscore.service.ItemLockService
import java.net.InetAddress

@Service
@Repository
@Transactional
class ItemLockServiceImpl(private val itemLockRepository: ItemLockRepository) : ItemLockService {
    private val hostName = InetAddress.getLocalHost().hostName

    override fun lock(): Boolean {
        val result = itemLockRepository.lock(hostName)

        return if (result == 1) {
            logger.info("Successfully acquired item's lock")
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
        val result = itemLockRepository.unlock(hostName)

        return if (result == 1) {
            logger.info("Successfully released item's lock")
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
        private const val LOCK_FAIL_MSG = "Cannot acquire item's lock. It was locked by another instance"
        private const val UNLOCK_FAIL_MSG = "Cannot release item's lock"

        private val logger = LoggerFactory.getLogger(ItemLockServiceImpl::class.java)
    }
}