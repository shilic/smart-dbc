package valueConverterTest

import io.github.shilic.smartDbc.valueConverter.*
import kotlin.test.Test
import kotlin.test.*

class SignalValueConverterTest {

    // ==================== findFirstIndexByValue ====================

    @Test
    fun `findFirstIndexByValue found`() {
        val vt = mapOf(0 to "预留", 1 to "关闭", 2 to "开启")
        assertEquals(2, vt.findFirstIndexByValue("开启"))
    }

    @Test
    fun `findFirstIndexByValue not found returns null`() {
        val vt = mapOf(0 to "预留", 1 to "关闭")
        assertNull(vt.findFirstIndexByValue("不存在"))
    }

    @Test
    fun `findFirstIndexByValue empty map`() {
        assertNull(emptyMap<Int, String>().findFirstIndexByValue("x"))
    }

    // ==================== phyToHex ====================

    @Test
    fun `phyToHex simple conversion`() {
        // phyValue = hexValue * 1.0 + 0.0  → hexValue = phyValue
        assertEquals(10L, 10.0.phyToHex(1.0, 0.0))
    }

    @Test
    fun `phyToHex with factor and offset`() {
        // phyValue = hexValue * 2.0 + (-10.0) → 30.0 = hexValue * 2.0 - 10.0 → hexValue = 20
        assertEquals(20L, 30.0.phyToHex(2.0, -10.0))
    }

    @Test
    fun `phyToHex truncates to long`() {
        // (10.7 - 0) / 1 = 10.7 → toLong() = 10
        assertEquals(10L, 10.7.phyToHex(1.0, 0.0))
    }

    @Test
    fun `phyToHex factor zero throws`() {
        val ex = assertFailsWith<IllegalStateException> { 10.0.phyToHex(0.0, 0.0) }
        assertTrue(ex.message!!.contains("factor作为除数不可以为0"))
    }

    // ==================== hexToPhy ====================

    @Test
    fun `hexToPhy simple`() {
        // phyValue = 10 * 1.0 + 0.0 = 10.0
        assertEquals(10.0, 10L.hexToPhy(1.0, 0.0))
    }

    @Test
    fun `hexToPhy with factor and offset`() {
        // phyValue = 20 * 0.5 + (-3.0) = 10.0 - 3.0 = 7.0
        assertEquals(7.0, 20L.hexToPhy(0.5, -3.0))
    }

    // ==================== phyToText ====================

    @Test
    fun `phyToText no value table returns formatted number`() {
        assertEquals("10.00", 10.0.phyToText(1.0, 0.0, emptyMap()))
    }

    @Test
    fun `phyToText with value table match`() {
        val vt = mapOf(0 to "预留", 1 to "关闭", 2 to "开启")
        // hexValue = (2.0 - 0) / 1.0 = 2
        assertEquals("开启", 2.0.phyToText(1.0, 0.0, vt))
    }

    @Test
    fun `phyToText with value table no match falls back to digits`() {
        val vt = mapOf(0 to "预留")
        // hexValue = 99
        assertEquals("99.00", 99.0.phyToText(1.0, 0.0, vt))
    }

    // ==================== textToPhy ====================

    @Test
    fun `textToPhy direct double no value table`() {
        assertEquals(3.14, "3.14".textToPhy(1.0, 0.0, emptyMap()))
    }

    @Test
    fun `textToPhy with value table match`() {
        val vt = mapOf(0 to "预留", 1 to "关闭", 2 to "开启")
        // "开启" → key=2 → phyValue = 2 * 1.0 + 0.0 = 2.0
        assertEquals(2.0, "开启".textToPhy(1.0, 0.0, vt))
    }

    @Test
    fun `textToPhy not in value table tries parseDouble`() {
        val vt = mapOf(0 to "预留")
        assertEquals(5.5, "5.5".textToPhy(1.0, 0.0, vt))
    }

    @Test
    fun `textToPhy invalid text throws`() {
        assertFailsWith<IllegalStateException> { "abc".textToPhy(1.0, 0.0, emptyMap()) }
    }

    // ==================== hexToText ====================

    @Test
    fun `hexToText no value table returns toString`() {
        assertEquals("42", 42.hexToText(emptyMap()))
    }

    @Test
    fun `hexToText with value table match`() {
        val vt = mapOf(0 to "关闭", 1 to "开启")
        assertEquals("开启", 1.hexToText(vt))
    }

    @Test
    fun `hexToText with value table no match falls back`() {
        val vt = mapOf(0 to "关闭")
        assertEquals("99", 99.hexToText(vt))
    }

    // ==================== textToHex ====================

    @Test
    fun `textToHex direct int no value table`() {
        assertEquals(42, "42".textToHex(emptyMap()))
    }

    @Test
    fun `textToHex with value table match`() {
        val vt = mapOf(0 to "预留", 1 to "关闭", 2 to "开启")
        assertEquals(2, "开启".textToHex(vt))
    }

    @Test
    fun `textToHex invalid throws`() {
        assertFailsWith<IllegalStateException> { "abc".textToHex(emptyMap()) }
    }
}
