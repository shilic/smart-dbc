package toolTest

import io.github.shilic.smartDbc.common.typeExtension.*
import kotlin.test.Test
import kotlin.test.*

class StringUtilsTest {

    // ==================== isWord ====================

    @Test fun `isWord valid lowercase`() = assertTrue("abc_def".isWord)
    @Test fun `isWord valid uppercase`() = assertTrue("ABC_DEF".isWord)
    @Test fun `isWord valid mixed`() = assertTrue("aBc_D3f".isWord)
    @Test fun `isWord starts with underscore`() = assertTrue("_private".isWord)
    @Test fun `isWord starts with letter`() = assertTrue("var1".isWord)
    @Test fun `isWord invalid starts with digit`() = assertFalse("1abc".isWord)
    @Test fun `isWord invalid contains hyphen`() = assertFalse("abc-def".isWord)
    @Test fun `isWord invalid contains dot`() = assertFalse("abc.def".isWord)
    @Test fun `isWord invalid contains spaces`() = assertFalse("abc def".isWord)
    @Test fun `isWord invalid empty string`() = assertFalse("".isWord)
    @Test fun `isWord invalid chinese chars`() = assertFalse("信号".isWord)

    // ==================== requireWord ====================

    @Test fun `requireWord valid passes`() = "valid_Name1".requireWord()
    @Test fun `requireWord invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "1bad".requireWord() }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    // ==================== isDecimal ====================

    @Test fun `isDecimal valid positive`() = assertTrue("123".isDecimal)
    @Test fun `isDecimal valid negative`() = assertTrue("-123".isDecimal)
    @Test fun `isDecimal valid with underscores`() = assertTrue("1_000_000".isDecimal)
    @Test fun `isDecimal valid zero`() = assertTrue("0".isDecimal)
    @Test fun `isDecimal invalid hex`() = assertFalse("0xFF".isDecimal)
    @Test fun `isDecimal invalid float`() = assertFalse("3.14".isDecimal)
    @Test fun `isDecimal invalid letters`() = assertFalse("abc".isDecimal)
    @Test fun `isDecimal invalid empty`() = assertFalse("".isDecimal)

    // ==================== requireDecimal ====================

    @Test fun `requireDecimal valid`() = "123".requireDecimal()
    @Test fun `requireDecimal invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "abc".requireDecimal() }
        assertTrue(ex.message!!.contains("不是一个十进制整数"))
    }

    // ==================== isHex ====================

    @Test fun `isHex valid`() = assertTrue("0xFF".isHex)
    @Test fun `isHex valid lowercase`() = assertTrue("0xab".isHex)
    @Test fun `isHex valid X`() = assertTrue("0XAB".isHex)
    @Test fun `isHex valid with underscores`() = assertTrue("0xFF_FF".isHex)
    @Test fun `isHex invalid no prefix`() = assertFalse("FF".isHex)
    @Test fun `isHex invalid letters`() = assertFalse("0xGG".isHex)
    @Test fun `isHex invalid empty`() = assertFalse("".isHex)

    // ==================== requireHex ====================

    @Test fun `requireHex valid`() = "0xFF".requireHex()
    @Test fun `requireHex invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "FF".requireHex() }
        assertTrue(ex.message!!.contains("不是一个十六进制数"))
    }

    // ==================== isInteger ====================

    @Test fun `isInteger decimal`() = assertTrue("123".isInteger)
    @Test fun `isInteger hex`() = assertTrue("0xFF".isInteger)
    @Test fun `isInteger negative decimal`() = assertTrue("-10".isInteger)
    @Test fun `isInteger float not integer`() = assertFalse("3.14".isInteger)

    // ==================== requireInteger ====================

    @Test fun `requireInteger valid`() = "0xFF".requireInteger()
    @Test fun `requireInteger invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "3.14".requireInteger() }
        assertTrue(ex.message!!.contains("不是一个有效的整数"))
    }

    // ==================== isDouble ====================

    @Test fun `isDouble valid integer`() = assertTrue("123".isDouble)
    @Test fun `isDouble valid float`() = assertTrue("3.14".isDouble)
    @Test fun `isDouble valid negative float`() = assertTrue("-3.14".isDouble)
    @Test fun `isDouble valid with underscores`() = assertTrue("1_000.5".isDouble)
    @Test fun `isDouble invalid letters`() = assertFalse("abc".isDouble)
    @Test fun `isDouble invalid hex`() = assertFalse("0xFF".isDouble)
    @Test fun `isDouble invalid empty`() = assertFalse("".isDouble)

    // ==================== requireDouble ====================

    @Test fun `requireDouble valid`() = "3.14".requireDouble()
    @Test fun `requireDouble invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "abc".requireDouble() }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    // ==================== startsAndEndsWith ====================

    @Test fun `startsAndEndsWith same prefix-suffix`() = assertTrue("\"hello\"".startsAndEndsWith("\""))
    @Test fun `startsAndEndsWith different prefix-suffix`() = assertTrue("(hello)".startsAndEndsWith("(", ")"))
    @Test fun `startsAndEndsWith not matching`() = assertFalse("hello".startsAndEndsWith("\""))
    @Test fun `startsAndEndsWith only prefix matches`() = assertFalse("(hello".startsAndEndsWith("(", ")"))

    // ==================== requireStartsAndEnds ====================

    @Test fun `requireStartsAndEnds valid`() = "\"hello\"".requireStartsAndEnds("\"")
    @Test fun `requireStartsAndEnds invalid throws`() {
        val ex = assertFailsWith<IllegalArgumentException> { "hello".requireStartsAndEnds("\"") }
        assertTrue(ex.message!!.contains("不是以") || ex.message!!.contains("包裹"))
    }
}
