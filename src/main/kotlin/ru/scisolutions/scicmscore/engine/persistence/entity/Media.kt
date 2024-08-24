package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "core_media")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
class Media(
    @Column(nullable = false)
    var filename: String,
    var label: String? = null,
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
