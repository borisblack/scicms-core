package ru.scisolutions.scicmscore.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import ru.scisolutions.scicmscore.persistence.entity.User as UserEntity

class User(
    username: String,
    password: String,
    authorities: Collection<GrantedAuthority>,
    val user: UserEntity
) : User(username, password, authorities)