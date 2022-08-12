package ru.scisolutions.scicmscore.util

import java.util.Locale

fun String.upperFirst() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.lowerFirst() = replaceFirstChar { it.lowercase(Locale.getDefault()) }