package ru.scisolutions.scicmscore.api.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.model.ChangePasswordRequest
import ru.scisolutions.scicmscore.model.RegistrationRequest
import ru.scisolutions.scicmscore.model.TokenResponse

@RestController
@RequestMapping("/api/auth/local")
class UserController(
    private val engine: Engine
) {
    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegistrationRequest): TokenResponse =
        engine.registerUser(registrationRequest)

    @PostMapping("/password")
    fun changePassword(@RequestBody changePasswordRequest: ChangePasswordRequest) =
        engine.changePassword(changePasswordRequest)
}