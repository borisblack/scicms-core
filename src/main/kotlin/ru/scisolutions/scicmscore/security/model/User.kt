package ru.scisolutions.scicmscore.security.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import ru.scisolutions.scicmscore.engine.persistence.entity.User as UserEntity

class User(
    username: String,
    password: String,
    authorities: Collection<GrantedAuthority>,
    val user: UserEntity? = null
) : User(username, password, authorities)