package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Sequence
import ru.scisolutions.scicmscore.persistence.repository.SequenceRepository
import ru.scisolutions.scicmscore.persistence.service.SequenceService

@Service
@Repository
@Transactional
class SequenceServiceImpl(private val sequenceRepository: SequenceRepository) : SequenceService {
    @Transactional(readOnly = true)
    override fun existsByName(name: String): Boolean = sequenceRepository.existsByName(name)

    @Transactional(readOnly = true)
    override fun getByName(name: String): Sequence = sequenceRepository.getByName(name)

    override fun nextByName(name: String): String {
        val sequence = getByName(name)
        val curValue = sequence.currentValue
        val nextValue = (curValue ?: sequence.initialValue) + sequence.step
        val rows =
            if (curValue == null)
                sequenceRepository.initCurrentValueByName(name, nextValue)
            else
                sequenceRepository.updateCurrentValueByName(name, curValue, nextValue)

        if (rows == 0)
            throw IllegalStateException("Cannot update sequence [$name] value. Please try again")

        val padWith = sequence.padWith
        val padTo = sequence.padTo
        val strNextValue = if (padWith == null || padTo == null) nextValue.toString() else nextValue.toString().padStart(padTo, padWith)

        return "${sequence.prefix ?: ""}${strNextValue}${sequence.suffix ?: ""}"
    }
}