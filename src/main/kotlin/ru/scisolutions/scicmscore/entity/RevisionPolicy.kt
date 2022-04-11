package ru.scisolutions.scicmscore.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_revision_policies")
class RevisionPolicy(
    @Column(nullable = false)
    val name: String,

    @Column(name = "display_name")
    val displayName: String?,

    @Column
    val revisions: String?,
) : AbstractEntity() {
    companion object {
        const val DEFAULT_REVISION_POLICY_ID: String = "48fea283-2872-4ca3-8fbd-980b7654907b"
    }
}