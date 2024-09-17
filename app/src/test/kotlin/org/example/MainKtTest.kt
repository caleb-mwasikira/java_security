package org.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*


class MainTest {
    @Test
    fun `should hash passwords correctly`(): Unit {
        val expectedHashValue: String = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        val actualHashValue: String = hashPassword("password")
        assertEquals(expectedHashValue, actualHashValue)
    }

    @Test
    fun `should convert byte arrays to valid hex values`(): Unit {
        val localeDefault = Locale.getDefault()

        // convert to lowercase as case matching is not important here.
        // we only care if the .toHex() fn produces a valid hex value
        val expectedHexValue: String = "68656C6C6F20776F726C64".lowercase(localeDefault)
        val actualHexValue: String = "hello world".toByteArray().toHex()
        assertEquals(expectedHexValue, actualHexValue.lowercase(localeDefault))
    }
}
