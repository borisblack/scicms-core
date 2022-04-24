package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_permissions")
class Permission(
    @Column(nullable = false)
    val name: String,
) : AbstractEntity() {
    companion object {
        const val DEFAULT_PERMISSION_ID: String = "6fd701bf-87e0-4aca-bbfd-fe1e9f85fc71"
    }
}