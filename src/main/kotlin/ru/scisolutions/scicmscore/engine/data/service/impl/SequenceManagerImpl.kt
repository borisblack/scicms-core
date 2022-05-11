package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.SequenceService

@Service
class SequenceManagerImpl(private val sequenceService: SequenceService) : SequenceManager {
    override fun assignSequenceAttributes(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == Type.sequence }
            .forEach { (attrName, attribute) ->
                val seqName = attribute.seqName ?: throw IllegalArgumentException("Sequence name is null")
                val nextValue = sequenceService.nextByName(seqName)
                itemRec[attrName] = nextValue
            }
    }
}