package nl.parlementairemonitor.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextUtilTest {
    @Test
    fun testMakeSearchableText() {
        assertEquals("israel", TextUtil.makeSearchableText("Israël"))
    }
}