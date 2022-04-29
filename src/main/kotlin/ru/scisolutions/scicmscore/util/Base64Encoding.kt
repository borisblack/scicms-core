package ru.scisolutions.scicmscore.util

import com.google.common.io.BaseEncoding

object Base64Encoding {
    fun encodeNullable(rawText: String?) = if (rawText.isNullOrBlank()) null else encode(rawText)

    fun encode(rawText: String) = BaseEncoding.base64().encode(rawText.toByteArray())

    fun decodeNullable(encodedText: String?) = if (encodedText.isNullOrBlank()) null else decode(encodedText)

    fun decode(encodedText: String) = BaseEncoding.base64().decode(encodedText).decodeToString()
}