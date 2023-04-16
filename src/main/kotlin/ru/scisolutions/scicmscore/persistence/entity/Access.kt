package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_access")
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
    @Type(type = "org.hibernate.type.NumericBooleanType")
    val granting: Boolean = true,

    @Column(name = "begin_date", nullable = false)
    val beginDate: OffsetDateTime,

    @Column(name = "end_date")
    val endDate: OffsetDateTime
) : AbstractEntity() {
    class AccessComparator: Comparator<Access> {
        override fun compare(left: Access, right: Access): Int {
            val res = (left.sortOrder ?: Int.MAX_VALUE).compareTo(right.sortOrder ?: Int.MAX_VALUE)
            if (res != 0)
                return res

            if (left.granting == right.granting)
                return 0

            return if (left.granting) 1 else -1
        }
    }
}