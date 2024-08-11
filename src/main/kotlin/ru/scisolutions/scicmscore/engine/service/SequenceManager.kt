package ru.scisolutions.scicmscore.engine.service

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.SequenceService

@Service
class SequenceManager(private val sequenceService: SequenceService) {
    fun assignSequenceAttributes(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == FieldType.sequence }
            .forEach { (attrName, attribute) ->
                val seqName = attribute.seqName ?: throw IllegalArgumentException("Sequence name is null")
                val nextValue = sequenceService.nextByName(seqName)
                itemRec[attrName] = nextValue
            }
    }
}
