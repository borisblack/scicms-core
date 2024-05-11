package ru.scisolutions.scicmscore.security

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission
import ru.scisolutions.scicmscore.engine.persistence.entity.User
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

@Component
class CustomUserDetailsManager(
    dataSource: DataSource,
    jdbcTemplate: JdbcTemplate
) : JdbcUserDetailsManager() {
    private val groupAuthoritiesByUsernameQuery = GROUP_AUTHORITIES_BY_USERNAME_QUERY.trimIndent()

    private val createUserSql = CREATE_USER_SQL.trimIndent()
    private val deleteUserSql = DELETE_USER_SQL.trimIndent()
    private val updateUserSql = UPDATE_USER_SQL.trimIndent()
    private val createAuthoritySql = INSERT_AUTHORITY_SQL.trimIndent()
    private val deleteUserAuthoritiesSql = DELETE_USER_AUTHORITIES_SQL.trimIndent()
    private val userExistsSql = USER_EXISTS_SQL.trimIndent()
    private val changePasswordSql = CHANGE_PASSWORD_SQL.trimIndent()

    private val findAllGroupsSql = FIND_GROUPS_SQL.trimIndent()
    private val findUsersInGroupSql = FIND_USERS_IN_GROUP_SQL.trimIndent()
    private val insertGroupSql = INSERT_GROUP_SQL.trimIndent()
    private val findGroupIdSql = FIND_GROUP_ID_SQL.trimIndent()
    private val insertGroupAuthoritySql = INSERT_GROUP_AUTHORITY_SQL.trimIndent()
    private val deleteGroupSql = DELETE_GROUP_SQL.trimIndent()
    private val deleteGroupAuthoritiesSql = DELETE_GROUP_AUTHORITIES_SQL.trimIndent()
    private val deleteGroupMembersSql = DELETE_GROUP_MEMBERS_SQL.trimIndent()
    private val renameGroupSql = RENAME_GROUP_SQL.trimIndent()
    private val insertGroupMemberSql = INSERT_GROUP_MEMBER_SQL.trimIndent()
    private val deleteGroupMemberSql = DELETE_GROUP_MEMBER_SQL.trimIndent()
    private val groupAuthoritiesSql = GROUP_AUTHORITIES_QUERY_SQL.trimIndent()
    private val deleteGroupAuthoritySql = DELETE_GROUP_AUTHORITY_SQL.trimIndent()

    private var userCache: UserCache = NullUserCache()

    init {
        setDataSource(dataSource)
        setJdbcTemplate(jdbcTemplate)

        enableGroups = true
        
        usersByUsernameQuery = USERS_BY_USERNAME_QUERY.trimIndent()
        authoritiesByUsernameQuery = AUTHORITIES_BY_USERNAME_QUERY.trimIndent()
        setGroupAuthoritiesByUsernameQuery(groupAuthoritiesByUsernameQuery)
        
        setCreateUserSql(createUserSql)
        setDeleteUserSql(deleteUserSql)
        setUpdateUserSql(updateUserSql)
        setCreateAuthoritySql(createAuthoritySql)
        setDeleteUserAuthoritiesSql(deleteUserAuthoritiesSql)
        setUserExistsSql(userExistsSql)
        setChangePasswordSql(changePasswordSql)
        setFindAllGroupsSql(findAllGroupsSql)
        setFindUsersInGroupSql(findUsersInGroupSql)
        setInsertGroupSql(insertGroupSql)
        setFindGroupIdSql(findGroupIdSql)
        setInsertGroupAuthoritySql(insertGroupAuthoritySql)
        setDeleteGroupSql(deleteGroupSql)
        setDeleteGroupAuthoritiesSql(deleteGroupAuthoritiesSql)
        setDeleteGroupMembersSql(deleteGroupMembersSql)
        setRenameGroupSql(renameGroupSql)
        setInsertGroupMemberSql(insertGroupMemberSql)
        setDeleteGroupMemberSql(deleteGroupMemberSql)
        setGroupAuthoritiesSql(groupAuthoritiesSql)
        setDeleteGroupAuthoritySql(deleteGroupAuthoritySql)
    }

    override fun addGroupAuthority(groupName: String, authority: GrantedAuthority) {
        logger.debug("Adding authority '$authority' to group '$groupName'")
        Assert.hasText(groupName, "groupName should have text")
        Assert.notNull(authority, "authority cannot be null")
        val newId = generateUUID()
        val groupId = findGroupId(groupName)
        val now = LocalDateTime.now()
        jdbcTemplate!!.update(
            insertGroupAuthoritySql, newId, newId, groupId, authority.authority,
            DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
            now, ROOT_USER_ID, now, ROOT_USER_ID
        )
    }

    private fun generateUUID() = UUID.randomUUID().toString()

    override fun removeGroupAuthority(groupName: String, authority: GrantedAuthority) {
        logger.debug("Removing authority '$authority' from group '$groupName'")
        Assert.hasText(groupName, "groupName should have text")
        Assert.notNull(authority, "authority cannot be null")
        val groupId = findGroupId(groupName)
        jdbcTemplate!!.update(deleteGroupAuthoritySql, groupId, authority.authority)
    }

    override fun addUserToGroup(username: String, groupName: String) {
        logger.debug("Adding user '$username' to group '$groupName'")
        Assert.hasText(username, "username should have text")
        Assert.hasText(groupName, "groupName should have text")
        val newId = generateUUID()
        val groupId = findGroupId(groupName)
        val now = LocalDateTime.now()
        jdbcTemplate!!.update(
            insertGroupMemberSql, newId, newId, groupId, username,
            DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
            now, ROOT_USER_ID, now, ROOT_USER_ID
        )
        userCache.removeUserFromCache(username)
    }

    override fun createUser(user: UserDetails) {
        validateUserDetails(user)
        val newId = generateUUID()
        val username = user.username
        val now = LocalDateTime.now()
        jdbcTemplate!!.update(
            createUserSql, newId, newId, username, user.password, if (user.isEnabled) 1 else 0,
            DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
            now, ROOT_USER_ID, now, ROOT_USER_ID
        )
        if (enableAuthorities)
            insertUserAuthorities(user)
    }

    private fun validateUserDetails(user: UserDetails) {
        Assert.hasText(user.username, "Username may not be empty or null")
        validateAuthorities(user.authorities)
    }

    private fun validateAuthorities(authorities: Collection<GrantedAuthority>) {
        Assert.notNull(authorities, "Authorities list must not be null")
        for (authority: GrantedAuthority in authorities) {
            Assert.notNull(authority, "Authorities list contains a null entry")
            Assert.hasText(
                authority.authority,
                "getAuthority() method must return a non-empty string"
            )
        }
    }

    private fun insertUserAuthorities(user: UserDetails) {
        val now = LocalDateTime.now()
        for (auth: GrantedAuthority in user.authorities) {
            val newId = generateUUID()
            jdbcTemplate!!.update(
                createAuthoritySql, newId, newId, user.username, auth.authority,
                DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
                now, ROOT_USER_ID, now, ROOT_USER_ID
            )
        }
    }

    override fun createGroup(
        groupName: String,
        authorities: List<GrantedAuthority>
    ) {
        Assert.hasText(groupName, "groupName should have text")
        Assert.notNull(authorities, "authorities cannot be null")
        logger.debug(
            ("Creating new group '" + groupName + "' with authorities "
                    + AuthorityUtils.authorityListToSet(authorities))
        )
        val newId = generateUUID()
        val now = LocalDateTime.now()
        jdbcTemplate!!.update(
            insertGroupSql, newId, newId, groupName,
            DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
            now, ROOT_USER_ID, now, ROOT_USER_ID
        )
        val groupId = findGroupId(groupName)
        for (a: GrantedAuthority in authorities) {
            val groupAuthorityId = generateUUID()
            val authority = a.authority
            jdbcTemplate!!.update(
                insertGroupAuthoritySql, groupAuthorityId, groupAuthorityId, groupId, authority,
                DEFAULT_GENERATION, DEFAULT_MAJOR_REV, if (DEFAULT_IS_CURRENT) 1 else 0, DEFAULT_PERMISSION_ID,
                now, ROOT_USER_ID, now, ROOT_USER_ID
            )
        }
    }

    override fun deleteGroup(groupName: String) {
        logger.debug("Deleting group '$groupName'")
        Assert.hasText(groupName, "groupName should have text")
        val groupId = findGroupId(groupName)
        jdbcTemplate!!.update(deleteGroupMembersSql, groupId)
        jdbcTemplate!!.update(deleteGroupAuthoritiesSql, groupId)
        jdbcTemplate!!.update(deleteGroupSql, groupId)
    }

    override fun removeUserFromGroup(username: String, groupName: String) {
        logger.debug("Removing user '$username' to group '$groupName'")
        Assert.hasText(username, "username should have text")
        Assert.hasText(groupName, "groupName should have text")
        val groupId = findGroupId(groupName)
        jdbcTemplate!!.update(deleteGroupMemberSql, groupId, username)
        userCache.removeUserFromCache(username)
    }

    private fun findGroupId(groupName: String): String =
        jdbcTemplate!!.queryForObject(findGroupIdSql, String::class.java, groupName)

    override fun setUserCache(userCache: UserCache) {
        super.setUserCache(userCache)
        this.userCache = userCache
    }

    @Throws(UsernameNotFoundException::class)
    fun loadUserByUsername(username: String, enableAuthorities: Boolean?, enableGroups: Boolean?): UserDetails {
        val users = loadUsersByUsername(username)
        if (users.size == 0) {
            logger.debug("Query returned no results for user '$username'")
            throw UsernameNotFoundException(
                messages.getMessage("JdbcDaoImpl.notFound", arrayOf(username), "Username {0} not found")
            )
        }
        val user = users[0] // contains no GrantedAuthority[]
        val dbAuthsSet: MutableSet<GrantedAuthority> = HashSet()
        if (enableAuthorities == true || (enableAuthorities == null && this.enableAuthorities)) {
            dbAuthsSet.addAll(loadUserAuthorities(user.username))
        }
        if (enableGroups == true || (enableGroups == null && this.enableGroups)) {
            dbAuthsSet.addAll(loadGroupAuthorities(user.username))
        }
        val dbAuths: List<GrantedAuthority> = ArrayList(dbAuthsSet)
        addCustomAuthorities(user.username, dbAuths)
        if (dbAuths.isEmpty()) {
            logger.debug("User '$username' has no authorities and will be treated as 'not found'")
            throw UsernameNotFoundException(
                messages.getMessage(
                    "JdbcDaoImpl.noAuthority", arrayOf(username), "User {0} has no GrantedAuthority"
                )
            )
        }
        return createUserDetails(username, user, dbAuths)
    }

    override fun updateUser(user: UserDetails) {
        validateUserDetails(user)
        jdbcTemplate!!.update(updateUserSql, user.password, if (user.isEnabled) 1 else 0, user.username)
        if (enableAuthorities) {
            deleteUserAuthorities(user.username)
            insertUserAuthorities(user)
        }
        userCache.removeUserFromCache(user.username)
    }

    private fun deleteUserAuthorities(username: String) {
        jdbcTemplate!!.update(deleteUserAuthoritiesSql, username)
    }

    companion object {
        private const val USERS_BY_USERNAME_QUERY =
            "SELECT username, passwd, enabled FROM sec_users WHERE LOWER(username) = LOWER(?)"

        private const val AUTHORITIES_BY_USERNAME_QUERY = """
            SELECT u.username, r.role
            FROM sec_users u
                INNER JOIN sec_roles r ON u.username = r.username
            WHERE u.username = ?"""

        private const val GROUP_AUTHORITIES_BY_USERNAME_QUERY = """
            SELECT g.id, g.group_name, gr.role
            FROM sec_groups g
                INNER JOIN sec_group_members gm ON g.id = gm.group_id
                INNER JOIN sec_group_roles gr ON g.id = gr.group_id
            WHERE gm.username = ?"""

        private const val CREATE_USER_SQL = """
            INSERT INTO sec_users (
                id, config_id, username, passwd, enabled, generation, major_rev, is_current, permission_id,
                created_at, created_by_id, updated_at, updated_by_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        private const val DELETE_USER_SQL = "DELETE FROM sec_users WHERE LOWER(username) = LOWER(?)"

        private const val UPDATE_USER_SQL = "UPDATE sec_users SET passwd = ?, enabled = ? WHERE LOWER(username) = LOWER(?)"

        private const val INSERT_AUTHORITY_SQL = """
            INSERT INTO sec_roles (
                id, config_id, username, role, generation, major_rev, is_current, permission_id,
                created_at, created_by_id, updated_at, updated_by_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        private const val DELETE_USER_AUTHORITIES_SQL = "DELETE FROM sec_roles WHERE username = ?"

        private const val USER_EXISTS_SQL = "SELECT username FROM sec_users WHERE LOWER(username) = LOWER(?)"

        private const val CHANGE_PASSWORD_SQL = "UPDATE sec_uses SET passwd = ? WHERE LOWER(username) = LOWER(?)"

        private const val FIND_GROUPS_SQL = "SELECT group_name FROM sec_groups"

        private const val FIND_USERS_IN_GROUP_SQL = """
            SELECT username FROM sec_group_members gm, sec_groups g
            WHERE gm.group_id = g.id AND g.group_name = ?"""

        private const val INSERT_GROUP_SQL = """
            INSERT INTO sec_groups (
                id, config_id, group_name, generation, major_rev, is_current, permission_id,
                created_at, created_by_id, updated_at, updated_by_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        private const val FIND_GROUP_ID_SQL = "SELECT id FROM sec_groups WHERE group_name = ?"

        private const val INSERT_GROUP_AUTHORITY_SQL = """
            INSERT INTO sec_group_roles (
                id, config_id, group_id, role, generation, major_rev, is_current, permission_id,
                created_at, created_by_id, updated_at, updated_by_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        private const val DELETE_GROUP_SQL = "DELETE FROM sec_groups WHERE id = ?"

        private const val DELETE_GROUP_AUTHORITIES_SQL = "DELETE FROM sec_group_roles WHERE group_id = ?"

        private const val DELETE_GROUP_MEMBERS_SQL = "DELETE FROM sec_group_members WHERE group_id = ?"

        private const val RENAME_GROUP_SQL = "UPDATE sec_groups set group_name = ? WHERE group_name = ?"

        private const val INSERT_GROUP_MEMBER_SQL = """
            INSERT INTO sec_group_members (
                id, config_id, group_id, username, generation, major_rev, is_current, permission_id,
                created_at, created_by_id, updated_at, updated_by_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

        private const val DELETE_GROUP_MEMBER_SQL =
            "DELETE FROM sec_group_members WHERE group_id = ? AND LOWER(username) = LOWER(?)"

        private const val GROUP_AUTHORITIES_QUERY_SQL = """
            SELECT g.id, g.group_name, gr.role
            FROM sec_groups g
                INNER JOIN sec_group_roles gr ON g.id = gr.group_id
            WHERE g.group_name = ?"""

        private const val DELETE_GROUP_AUTHORITY_SQL = "DELETE FROM sec_group_roles WHERE group_id = ? AND role = ?"

        private const val DEFAULT_GENERATION = 1
        private const val DEFAULT_MAJOR_REV = "A"
        private const val DEFAULT_IS_CURRENT = true
        private const val DEFAULT_PERMISSION_ID = Permission.DEFAULT_PERMISSION_ID
        private const val ROOT_USER_ID = User.ROOT_USER_ID
    }
}