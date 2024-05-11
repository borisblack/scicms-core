package ru.scisolutions.scicmscore.extension

import java.util.*

fun String.upperFirst() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.lowerFirst() = replaceFirstChar { it.lowercase(Locale.getDefault()) }

fun String.isUUID(): Boolean =
    try {
        UUID.fromString(this)
        true
    } catch (e: IllegalArgumentException) {
        false
    }