package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_media")
class Media(
    @Column(nullable = false)
    var filename: String,

    @Column(name = "display_name")
    var displayName: String? = null,

    var description: String? = null,

    @Column(name = "file_size", nullable = false)
    var fileSize: Long,

    @Column(nullable = false)
    var mimetype: String,

    @Column(nullable = false)
    var path: String,

    @Column(nullable = false)
    var checksum: String
) : AbstractEntity()