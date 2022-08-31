package ru.scisolutions.scicmscore.persistence.entity

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_permissions")
class Permission(
    @Column(nullable = false)
    var name: String,
) : AbstractEntity() {
    companion object {
        val DEFAULT_PERMISSION_ID: UUID = UUID.fromString("6fd701bf-87e0-4aca-bbfd-fe1e9f85fc71")
    }
}