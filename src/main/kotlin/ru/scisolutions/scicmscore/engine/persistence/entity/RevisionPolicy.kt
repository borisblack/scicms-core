package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.engine.persistence.converter.RevisionsConverter

@Entity
@Table(name = "core_revision_policies")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
class RevisionPolicy(
    @Column(nullable = false)
    var name: String,
    @Column(name = "display_name")
    var displayName: String?,
    @Column(nullable = false)
    @Convert(converter = RevisionsConverter::class)
    var revisions: List<String>
) : AbstractEntity() {
    fun firstRevision(): String {
        if (revisions.isEmpty()) {
            throw IllegalStateException("Revision list is empty")
        }

        return revisions[0]
    }

    fun nextRevision(currentRevision: String): String {
        if (revisions.isEmpty()) {
            throw IllegalStateException("Revision list is empty")
        }

        for (i in revisions.indices) {
            if (revisions[i] == currentRevision) {
                if (i < revisions.size - 1) {
                    return revisions[i + 1]
                } else {
                    throw IllegalStateException("Revision [$currentRevision] is last in revision policy [$name]")
                }
            }
        }

        throw IllegalArgumentException("Revision [$currentRevision] not found in revision policy [$name]")
    }

    companion object {
        const val DEFAULT_REVISION_POLICY_ID: String = "48fea283-2872-4ca3-8fbd-980b7654907b"
    }
}
