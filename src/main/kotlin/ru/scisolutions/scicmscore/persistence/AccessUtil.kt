package ru.scisolutions.scicmscore.persistence

object AccessUtil {
    const val PERMISSION_IDS_SELECT_SNIPPET =
        "SELECT acc.source_id " +
        "FROM sec_access acc " +
            "INNER JOIN sec_identities sid ON acc.target_id = sid.id " +
        "WHERE acc.mask IN :mask " +
            "AND acc.begin_date <= {fn NOW()} " +
            "AND (acc.end_date IS NULL OR acc.end_date > {fn NOW()}) " +
            "AND (" +
                "(sid.principal = 1 AND sid.name = :username) OR (sid.principal = 0 AND sid.name IN :roles)" +
            ")"

    val READ_MASK = setOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)
    val WRITE_MASK = setOf(2, 3, 6, 7, 10, 11, 14, 15, 18, 19, 22, 23, 26, 27, 30, 31)
    val CREATE_MASK = setOf(4, 5, 6, 7, 12, 13, 14, 15, 20, 21, 22, 23, 28, 29, 30, 31)
    val DELETE_MASK = setOf(8, 9, 10, 11, 12, 13, 14, 15, 24, 25, 26, 27, 28, 29, 30 ,31)
    val ADMINISTRATION_MASK = setOf(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
}