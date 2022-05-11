package ru.scisolutions.scicmscore.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Sequence
import ru.scisolutions.scicmscore.persistence.repository.SequenceRepository
import ru.scisolutions.scicmscore.service.SequenceService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class SequenceServiceImpl(
    dataProps: DataProps,
    private val sequenceRepository: SequenceRepository
) : SequenceService {
    private val sequenceCache: Cache<String, Sequence> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.sequenceCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun getByName(name: String): Sequence = sequenceCache.get(name) { sequenceRepository.getByName(name) }

    override fun nextByName(name: String): String {
        val sequence = getByName(name)
        val nextValue = (sequence.currentValue ?: sequence.initialValue) + sequence.step
        val rows = sequenceRepository.updateCurrentValueByName(name, sequence.currentValue, nextValue)
        if (rows == 0)
            throw IllegalStateException("Cannot update sequence [$name] value. Please try again")

        val padWith = sequence.padWith
        val padTo = sequence.padTo
        val strNextValue = if (padWith == null || padTo == null) nextValue.toString() else nextValue.toString().padStart(padTo, padWith)

        return "${sequence.prefix ?: ""}${strNextValue}${sequence.suffix ?: ""}"
    }
}