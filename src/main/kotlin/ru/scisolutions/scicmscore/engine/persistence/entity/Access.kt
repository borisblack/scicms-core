package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime

/*@NamedNativeQuery(
    name = "Access.findAllByMask",
    query = Acl.ACCESS_SELECT_SNIPPET,
    resultClass = Access::class,
)*/
@Entity
@Table(name = "sec_access")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
class Access(
    val label: String?,
    @Column(name = "sort_order")
    val sortOrder: Int? = null,
    @Column(name = "source_id", nullable = false)
    val sourceId: String,
    @Column(name = "target_id", nullable = false)
    val targetId: String,
    @Column(nullable = false)
    val mask: Int = 0,
    @Column(nullable = false, columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    val granting: Boolean = true,
    @Column(name = "begin_date", nullable = false)
    val beginDate: OffsetDateTime,
    @Column(name = "end_date")
    val endDate: OffsetDateTime
) : AbstractEntity() {
    class AccessComparator : Comparator<Access> {
        override fun compare(left: Access, right: Access): Int {
            val res = (left.sortOrder ?: Int.MAX_VALUE).compareTo(right.sortOrder ?: Int.MAX_VALUE)
            if (res != 0) {
                return res
            }

            if (left.granting == right.granting) {
                return 0
            }

            return if (left.granting) 1 else -1
        }
    }
}
