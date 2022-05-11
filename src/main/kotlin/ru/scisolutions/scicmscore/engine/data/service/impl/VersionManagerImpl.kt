package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.VersionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.service.RevisionPolicyService

@Service
class VersionManagerImpl(private val revisionPolicyService: RevisionPolicyService) : VersionManager {
    override fun assignVersionAttributes(item: Item, itemRec: ItemRec, majorRev: String?) {
        if (item.versioned && item.manualVersioning) {
            if (majorRev == null)
                throw IllegalArgumentException("Attribute [majorRev] is required")

            itemRec.majorRev = majorRev
        } else {
            val revisionPolicyId = item.revisionPolicyId ?: RevisionPolicy.DEFAULT_REVISION_POLICY_ID
            val revisionPolicy = revisionPolicyService.getById(revisionPolicyId)
            itemRec.majorRev = revisionPolicy.firstRevision()
        }

        with(itemRec) {
            generation = 1
            lastVersion = true
            current = true
        }
    }

    override fun assignVersionAttributes(item: Item, prevItemRec: ItemRec, itemRec: ItemRec, majorRev: String?) {
        if (!item.versioned)
            throw IllegalArgumentException("Item is not versioned")

        if (item.manualVersioning) {
            if (majorRev == null)
                throw IllegalArgumentException("Attribute [majorRev] is required")

            itemRec.majorRev = majorRev
        } else {
            val prevMajorRev = prevItemRec.majorRev ?: throw IllegalArgumentException("Previous majorRev is null")
            val revisionPolicyId = item.revisionPolicyId ?: RevisionPolicy.DEFAULT_REVISION_POLICY_ID
            val revisionPolicy = revisionPolicyService.getById(revisionPolicyId)
            itemRec.majorRev = revisionPolicy.nextRevision(prevMajorRev)
        }

        with(itemRec) {
            generation = 1
            lastVersion = true
            current = true
        }
    }
}