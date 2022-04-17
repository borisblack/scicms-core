package ru.scisolutions.scicmscore.security.service

import org.springframework.security.core.GrantedAuthority

interface UserGroupManager {
    /**
     * Creates the user and adds him to the group.
     */
    fun createUser(username: String, groupName: String)

    /**
     * Creates the user.
     */
    fun createUser(username: String, password: String, enabled: Boolean, authorities: Collection<String>)

    /**
     * Creates the user, adds him to the group and authenticate.
     */
    fun createAndAuthenticateUser(username: String, groupName: String)

    /**
     * Creates the user with authority.
     */
    fun createUserWithAuthority(username: String, authority: String)

    fun createGroup(groupName: String, authorities: Collection<String>)

    /**
     * Returns group list.
     */
    fun listAllGroups(): List<String>

    /**
     * Returns users in group.
     */
    fun listGroupUsers(groupName: String): List<String>

    /**
     * Returns group authorities.
     */
    fun listGroupAuthorities(groupName: String): List<GrantedAuthority>

    fun addUserToGroup(username: String, groupName: String)

    fun removeUserFromGroup(username: String, groupName: String)

    fun deleteUser(username: String)

    fun deleteGroup(groupName: String)

    fun addUserAuthority(username: String, authority: GrantedAuthority)

    fun removeUserAuthority(username: String, authority: GrantedAuthority)

    fun addGroupAuthority(groupName: String, authority: GrantedAuthority)

    fun removeGroupAuthority(groupName: String, authority: GrantedAuthority)

    /**
     * Authenticates an existing user
     */
    fun setAuthentication(username: String)
}