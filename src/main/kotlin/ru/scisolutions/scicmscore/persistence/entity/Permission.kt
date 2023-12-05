package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "sec_permissions")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
class Permission(
    @Column(nullable = false)
    var name: String,
) : AbstractEntity() {
    companion object {
        const val DEFAULT_PERMISSION_ID: String = "6fd701bf-87e0-4aca-bbfd-fe1e9f85fc71"
        const val SECURITY_PERMISSION_ID: String = "4e1f310d-570f-4a16-9f41-cbc80b08ab8e"
        const val BI_PERMISSION_ID: String = "874e089e-cd9a-428a-962f-0c3d994cd371"
    }
}