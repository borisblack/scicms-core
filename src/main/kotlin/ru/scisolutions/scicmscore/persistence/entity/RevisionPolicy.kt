package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_revision_policies")
class RevisionPolicy(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String?,

    @Column
    var revisions: String?,
) : AbstractEntity() {
    companion object {
        const val DEFAULT_REVISION_POLICY_ID: String = "48fea283-2872-4ca3-8fbd-980b7654907b"
    }
}