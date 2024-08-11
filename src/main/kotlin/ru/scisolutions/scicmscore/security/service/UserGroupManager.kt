package ru.scisolutions.scicmscore.security.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

interface UserGroupManager {
    /**
     * Creates the user.
     */
    fun createUser(username: String, rawPassword: String, authorities: Set<String>)

    /**
     * Creates the user and adds him to the groups.
     */
    fun createUserInGroups(username: String, rawPassword: String, groupNames: Set<String>)

    fun loadUserByUsername(username: String): UserDetails

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
