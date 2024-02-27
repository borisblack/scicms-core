package ru.scisolutions.scicmscore.extension

import java.util.*

fun String.isUUID(): Boolean =
    try {
        UUID.fromString(this)
        true
    } catch (e: IllegalArgumentException) {
        false
    }