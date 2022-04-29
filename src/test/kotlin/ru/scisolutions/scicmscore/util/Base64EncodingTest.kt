package ru.scisolutions.scicmscore.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class Base64EncodingTest {
    @Test
    fun testEncode() {
        val encodedText = Base64Encoding.encodeNullable(RAW_TEXT)
        assertEquals(ENCODED_TEXT, encodedText)
    }

    @Test
    fun testDecode() {
        val rawText = Base64Encoding.decode(ENCODED_TEXT)
        assertEquals(RAW_TEXT, rawText)
    }

    @Test
    fun testEncodeNullableNotNull() {
        val encodedText = Base64Encoding.encodeNullable(RAW_TEXT)
        assertEquals(ENCODED_TEXT, encodedText)
    }

    @Test
    fun testDecodeNullableNotNull() {
        val rawText = Base64Encoding.decodeNullable(ENCODED_TEXT)
        assertEquals(RAW_TEXT, rawText)
    }

    @Test
    fun testEncodeNullableNull() {
        val encodedText = Base64Encoding.encodeNullable(null)
        assertNull(encodedText)
    }

    @Test
    fun testDecodeNullableNull() {
        val rawText = Base64Encoding.decodeNullable(null)
        assertNull(rawText)
    }

    companion object {
        const val RAW_TEXT = "rawText@1"
        const val ENCODED_TEXT = "cmF3VGV4dEAx"
    }
}