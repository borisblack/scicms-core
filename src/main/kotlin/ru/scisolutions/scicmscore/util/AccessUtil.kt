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

    fun getPermissionIdsForReadStatement() = getPermissionIdsStatement(AccessMask.READ)

    fun getPermissionIdsForWriteStatement() = getPermissionIdsStatement(AccessMask.WRITE)

    fun getPermissionIdsForCreateStatement() = getPermissionIdsStatement(AccessMask.CREATE)

    fun getPermissionIdsForDeleteStatement() = getPermissionIdsStatement(AccessMask.DELETE)

    fun getPermissionIdsForAdministrationStatement() = getPermissionIdsStatement(AccessMask.ADMINISTRATION)

    fun getPermissionIdsStatement(accessMask: AccessMask): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        val authorities = authentication.authorities
        val roles = AuthorityUtils.authorityListToSet(authorities)

        return PERMISSION_IDS_SELECT_SNIPPET
            .replace(":$MASK_PARAM_NAME", "(${accessMask.mask.joinToString()})")
            .replace(":$USERNAME_PARAM_NAME", "$QUOTE${username}$QUOTE")
            .replace(":$ROLES_PARAM_NAME", "(${roles.joinToString { "$QUOTE${it}$QUOTE" }})")
    }
}