package ru.scisolutions.scicmscore.service.impl

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
        return result == 1
    }

    override fun unlock(): Boolean {
        val result = itemLockRepository.unlock(hostName)
        return result == 1
    }
}