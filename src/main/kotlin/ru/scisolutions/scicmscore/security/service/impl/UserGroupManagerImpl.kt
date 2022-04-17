package ru.scisolutions.scicmscore.security.service.impl

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.security.CustomUserDetailsManager
import ru.scisolutions.scicmscore.security.service.UserGroupManager

@Service
@Transactional
class UserGroupManagerImpl(private val customUserDetailsManager: CustomUserDetailsManager) : UserGroupManager {
    override fun createUser(username: String, groupName: String) {
        // Create auth if it does not exist
        if (!customUserDetailsManager.userExists(username)) {
            val userDetails: UserDetails = User(username, username, emptyList())
            customUserDetailsManager.createUser(userDetails)
        }

        // Create group if it does not exist
        val allGroups: List<String> = customUserDetailsManager.findAllGroups()
        if (!allGroups.contains(groupName)) {
            val role = if (groupName == GROUP_ADMINISTRATORS) ROLE_ADMIN else ROLE_ANONYMOUS
            val grantedAuthorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority(role))
            customUserDetailsManager.createGroup(groupName, grantedAuthorities)
        }
        addUserToGroup(username, groupName)
    }

    override fun createUser(username: String, password: String, enabled: Boolean, authorities: Collection<String>) {
        if (!customUserDetailsManager.userExists(username)) {
            val grantedAuthorities = authorities.map { SimpleGrantedAuthority(it) }
            val userDetails: UserDetails = User(username, password, enabled, true, true, true, grantedAuthorities)
            customUserDetailsManager.createUser(userDetails)
        } else
            throw IllegalArgumentException("User already exists.")
    }

    override fun createAndAuthenticateUser(username: String, groupName: String) {
        createUser(username, groupName)
        setAuthentication(username)
    }

    override fun createUserWithAuthority(username: String, authority: String) {
        if (!customUserDetailsManager.userExists(username)) {
            val grantedAuthorities = listOf(SimpleGrantedAuthority(authority))
            val userDetails: UserDetails = User(username, username, grantedAuthorities)
            customUserDetailsManager.createUser(userDetails)
        }
    }

    override fun createGroup(groupName: String, authorities: Collection<String>) {
        val allGroups: List<String> = customUserDetailsManager.findAllGroups()
        if (!allGroups.contains(groupName)) {
            val grantedAuthorities = authorities.map { SimpleGrantedAuthority(it) }
            customUserDetailsManager.createGroup(groupName, grantedAuthorities)
        }
    }

    override fun listAllGroups(): List<String> = customUserDetailsManager.findAllGroups()

    override fun listGroupUsers(groupName: String): List<String> = customUserDetailsManager.findUsersInGroup(groupName)

    override fun listGroupAuthorities(groupName: String): List<GrantedAuthority> = customUserDetailsManager.findGroupAuthorities(groupName)

    override fun addUserToGroup(username: String, groupName: String) {
        val userExistInGroup: Boolean = customUserDetailsManager.findUsersInGroup(groupName).contains(username)
        if (!userExistInGroup)
            customUserDetailsManager.addUserToGroup(username, groupName)
    }

    override fun removeUserFromGroup(username: String, groupName: String) {
        val userExistInGroup: Boolean = customUserDetailsManager.findUsersInGroup(groupName).contains(username)
        if (userExistInGroup)
            customUserDetailsManager.removeUserFromGroup(username, groupName)
    }

    override fun deleteUser(username: String) = customUserDetailsManager.deleteUser(username)

    override fun deleteGroup(groupName: String) = customUserDetailsManager.deleteGroup(groupName)

    override fun addUserAuthority(username: String, authority: GrantedAuthority) {
        val userDetails = customUserDetailsManager.loadUserByUsername(username, true, false) // load only own authorities
        val authorities: MutableSet<GrantedAuthority> = HashSet(userDetails.authorities)
        authorities.add(authority)
        val userBuilder = User.withUserDetails(userDetails)
        val newDetails = userBuilder.authorities(authorities).build()
        customUserDetailsManager.updateUser(newDetails)
    }

    override fun removeUserAuthority(username: String, authority: GrantedAuthority) {
        val userDetails = customUserDetailsManager.loadUserByUsername(username, true, false) // load only own authorities
        val authorities: MutableSet<GrantedAuthority> = HashSet(userDetails.authorities)
        authorities.remove(authority)
        val userBuilder = User.withUserDetails(userDetails)
        val newDetails = userBuilder.authorities(authorities).build()
        customUserDetailsManager.updateUser(newDetails)
    }

    override fun addGroupAuthority(groupName: String, authority: GrantedAuthority) = customUserDetailsManager.addGroupAuthority(groupName, authority)

    override fun removeGroupAuthority(groupName: String, authority: GrantedAuthority) = customUserDetailsManager.removeGroupAuthority(groupName, authority)

    override fun setAuthentication(username: String) {
        val user: UserDetails = customUserDetailsManager.loadUserByUsername(username)
        val authentication = UsernamePasswordAuthenticationToken(user.username, user.password, user.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    companion object {
        private const val GROUP_ADMINISTRATORS = "Administrators"
        private const val ROLE_ADMIN = "ROLE_ADMIN"
        private const val ROLE_ANONYMOUS = "ROLE_ANONYMOUS"
    }
}