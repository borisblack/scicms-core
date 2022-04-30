package ru.scisolutions.scicmscore.util

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

object AccessUtil {
    private const val QUOTE = "'"
    const val MASK_PARAM_NAME = "mask"
    const val USERNAME_PARAM_NAME = "username"
    const val ROLES_PARAM_NAME = "roles"

    const val PERMISSION_IDS_SELECT_SNIPPET =
        "SELECT acc.source_id " +
        "FROM sec_access acc " +
            "INNER JOIN sec_identities sid ON acc.target_id = sid.id " +
        "WHERE acc.mask IN :$MASK_PARAM_NAME " +
            "AND acc.begin_date <= {fn NOW()} " +
            "AND (acc.end_date IS NULL OR acc.end_date > {fn NOW()}) " +
            "AND (" +
                "(sid.principal = 1 AND sid.name = :$USERNAME_PARAM_NAME) OR (sid.principal = 0 AND sid.name IN :$ROLES_PARAM_NAME)" +
            ")"

    val READ_MASK = setOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)
    val WRITE_MASK = setOf(2, 3, 6, 7, 10, 11, 14, 15, 18, 19, 22, 23, 26, 27, 30, 31)
    val CREATE_MASK = setOf(4, 5, 6, 7, 12, 13, 14, 15, 20, 21, 22, 23, 28, 29, 30, 31)
    val DELETE_MASK = setOf(8, 9, 10, 11, 12, 13, 14, 15, 24, 25, 26, 27, 28, 29, 30 ,31)
    val ADMINISTRATION_MASK = setOf(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)

    fun getPermissionIdsForReadStatement() = getPermissionIdsStatement(READ_MASK)

    fun getPermissionIdsForWriteStatement() = getPermissionIdsStatement(WRITE_MASK)

    fun getPermissionIdsForCreateStatement() = getPermissionIdsStatement(CREATE_MASK)

    fun getPermissionIdsForDeleteStatement() = getPermissionIdsStatement(DELETE_MASK)

    fun getPermissionIdsForAdministrationStatement() = getPermissionIdsStatement(ADMINISTRATION_MASK)

    fun getPermissionIdsStatement(mask: Set<Int>): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        val authorities = authentication.authorities
        val roles = AuthorityUtils.authorityListToSet(authorities)

        return PERMISSION_IDS_SELECT_SNIPPET
            .replace(":$MASK_PARAM_NAME", "(${mask.joinToString()})")
            .replace(":$USERNAME_PARAM_NAME", "$QUOTE${username}$QUOTE")
            .replace(":$ROLES_PARAM_NAME", "(${roles.joinToString { "$QUOTE${it}$QUOTE" }})")
    }
}