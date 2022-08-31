package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.persistence.converter.RevisionsConverter
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_revision_policies")
class RevisionPolicy(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String?,

    @Column(nullable = false)
    @Convert(converter = RevisionsConverter::class)
    var revisions: List<String>,
) : AbstractEntity() {
    fun firstRevision(): String {
        if (revisions.isEmpty())
            throw IllegalStateException("Revision list is empty")

        return revisions[0]
    }

    fun nextRevision(currentRevision: String): String {
        if (revisions.isEmpty())
            throw IllegalStateException("Revision list is empty")

        for (i in revisions.indices) {
            if (revisions[i] == currentRevision) {
                if (i < revisions.size - 1)
                    return revisions[i + 1]
                else
                    throw IllegalStateException("Revision [$currentRevision] is last in revision policy [$name]")
            }
        }

        throw IllegalArgumentException("Revision [$currentRevision] not found in revision policy [$name]")
    }

    companion object {
        val DEFAULT_REVISION_POLICY_ID: UUID = UUID.fromString("48fea283-2872-4ca3-8fbd-980b7654907b")
    }
}