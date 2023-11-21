package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Sequence
import ru.scisolutions.scicmscore.persistence.repository.SequenceRepository

@Service
@Repository
@Transactional
class SequenceService(private val sequenceRepository: SequenceRepository) {
    @Transactional(readOnly = true)
    fun existsByName(name: String): Boolean = sequenceRepository.existsByName(name)

    @Transactional(readOnly = true)
    fun getByName(name: String): Sequence = sequenceRepository.getByName(name)

    fun nextByName(name: String): String {
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