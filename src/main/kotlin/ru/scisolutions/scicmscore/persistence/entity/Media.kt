package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_media")
class Media(
    @Column(nullable = false)
    val filename: String,

    @Column(name = "display_name")
    val displayName: String? = null,

    val description: String? = null,

    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    @Column(nullable = false)
    val mimetype: String,

    @Column(nullable = false)
    val path: String,

    @Column(nullable = false)
    val checksum: String
) : AbstractEntity()