package ru.scisolutions.scicmscore.model

class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)