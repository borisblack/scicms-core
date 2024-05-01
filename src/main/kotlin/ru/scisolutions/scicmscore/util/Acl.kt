package ru.scisolutions.scicmscore.util

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.charset.Charset
import java.util.*

object Acl {
    private const val QUOTE = "'"
    private const val MASK_PARAM_NAME = "mask"
    private const val USERNAME_PARAM_NAME = "username"
    private const val ROLES_PARAM_NAME = "roles"

    const val ROLE_ADMIN = "ROLE_ADMIN"
    const val ROLE_ANALYST = "ROLE_ANALYST"
    const val ROLE_ANONYMOUS = "ROLE_ANONYMOUS"
    const val GROUP_ADMINISTRATORS = "Administrators"
    const val GROUP_USERS = "Users"

    const val ACCESS_SELECT_SNIPPET =
        "SELECT acc.* " +
        "FROM sec_access acc " +
            "INNER JOIN sec_identities sid " +
                "ON acc.target_id = sid.id AND ((sid.principal = 1 AND sid.name = :$USERNAME_PARAM_NAME) OR (sid.principal = 0 AND sid.name IN :$ROLES_PARAM_NAME))" +
        "WHERE acc.mask IN :$MASK_PARAM_NAME " +
            "AND acc.begin_date <= {fn NOW()} " +
            "AND (acc.end_date IS NULL OR acc.end_date > {fn NOW()}) " +
        "ORDER BY acc.sort_order ASC, acc.granting DESC"

    const val ACCESS_JPQL_SNIPPET =
        "select acc " +
        "from Access acc " +
            "inner join Identity sid " +
                "on acc.targetId = sid.id and ((sid.principal = true and sid.name = :username) or (sid.principal = false and sid.name in :roles)) " +
        "where acc.mask in :mask " +
            "and acc.beginDate <= current_timestamp " +
            "and (acc.endDate is null or acc.endDate > current_timestamp) " +
        "order by acc.sortOrder asc, acc.granting desc"

    private const val PERMISSION_IDS_SELECT_SNIPPET =
        "SELECT DISTINCT acc.source_id " +
            "FROM sec_access acc " +
            "INNER JOIN sec_identities sid " +
            "ON acc.target_id = sid.id AND ((sid.principal = 1 AND sid.name = :$USERNAME_PARAM_NAME) OR (sid.principal = 0 AND sid.name IN :$ROLES_PARAM_NAME))" +
            "WHERE acc.mask IN :$MASK_PARAM_NAME " +
            "AND acc.granting = 1 " +
            "AND acc.begin_date <= {fn NOW()} " +
            "AND (acc.end_date IS NULL OR acc.end_date > {fn NOW()})"

    private const val RANDOM_PASSWORD_LENGTH = 7

    enum class Mask(val mask: Set<Int>) {
        READ(setOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)),
        WRITE(setOf(2, 3, 6, 7, 10, 11, 14, 15, 18, 19, 22, 23, 26, 27, 30, 31)),
        CREATE(setOf(4, 5, 6, 7, 12, 13, 14, 15, 20, 21, 22, 23, 28, 29, 30, 31)),
        DELETE(setOf(8, 9, 10, 11, 12, 13, 14, 15, 24, 25, 26, 27, 28, 29, 30, 31)),
        ADMINISTRATION(setOf(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31))
    }

    private fun getPermissionIdsForReadStatement() = getPermissionIdsStatement(Mask.READ)

    private fun getPermissionIdsForWriteStatement() = getPermissionIdsStatement(Mask.WRITE)

    private fun getPermissionIdsForCreateStatement() = getPermissionIdsStatement(Mask.CREATE)

    private fun getPermissionIdsForDeleteStatement() = getPermissionIdsStatement(Mask.DELETE)

    private fun getPermissionIdsForAdministrationStatement() = getPermissionIdsStatement(Mask.ADMINISTRATION)

    private fun getPermissionIdsStatement(accessMask: Mask): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        val username = authentication.name
        val authorities = authentication.authorities
        val roles = AuthorityUtils.authorityListToSet(authorities)

        return PERMISSION_IDS_SELECT_SNIPPET
            .replace(":$MASK_PARAM_NAME", "(${accessMask.mask.joinToString()})")
            .replace(":$USERNAME_PARAM_NAME", "$QUOTE${username}$QUOTE")
            .replace(":$ROLES_PARAM_NAME", "(${roles.joinToString { "$QUOTE${it}$QUOTE" }})")
    }

    fun getRoles(): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        val authorities = authentication.authorities

        return AuthorityUtils.authorityListToSet(authorities)
    }

    fun randomPassword(): String {
        val array = ByteArray(RANDOM_PASSWORD_LENGTH)
        Random().nextBytes(array)
        return String(array, Charset.forName("UTF-8"))
    }
}