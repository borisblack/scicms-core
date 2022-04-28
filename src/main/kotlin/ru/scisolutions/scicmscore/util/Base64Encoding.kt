package ru.scisolutions.scicmscore.util

import com.google.common.io.BaseEncoding

object Base64Encoding {
    fun encodeOrNull(rawText: String?) =
        if (rawText == null)
            null
        else
            try {
                encode(rawText)
            } catch (e: Exception) {
                rawText
            }

    fun encode(rawText: String) = BaseEncoding.base64().encode(rawText.toByteArray())

    fun decodeOrNull(encodedText: String?) =
        if (encodedText == null) null
        else
            try {
                decode(encodedText)
            } catch (e: Exception) {
                encodedText
            }

    fun decode(encodedText: String) = BaseEncoding.base64().decode(encodedText)
}