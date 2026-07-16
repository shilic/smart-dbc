package toolTest

import io.github.shilic.smartDbc.common.typeExtension.digitsFormat
import io.github.shilic.smartDbc.common.typeExtension.toDoubleValue
import io.github.shilic.smartDbc.common.typeExtension.toPropertyValue
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.*

class NumberUtilsTest {

    // ==================== digitsFormat ====================

    @Test
    fun `digitsFormat default 2 digits`() {
        assertEquals("3.14", 3.14159.digitsFormat())
    }

    @Test
    fun `digitsFormat 4 digits`() {
        assertEquals("3.1416", 3.14159.digitsFormat(4))
    }

    @Test
    fun `digitsFormat zero digits`() {
        assertEquals("3", 3.14.digitsFormat(0))
    }

    @Test
    fun `digitsFormat integer`() {
        assertEquals("5.00", 5.0.digitsFormat())
    }

    // ==================== toPropertyValue ====================

    @Test
    fun `toPropertyValue Byte`() = assertEquals(42.toByte(), 42.0.toPropertyValue(Byte::class))
    @Test
    fun `toPropertyValue Short`() = assertEquals(100.toShort(), 100.0.toPropertyValue(Short::class))
    @Test
    fun `toPropertyValue Int`() = assertEquals(42, 42.5.toPropertyValue(Int::class))
    @Test
    fun `toPropertyValue Long`() = assertEquals(42L, 42.0.toPropertyValue(Long::class))
    @Test
    fun `toPropertyValue Float`() = assertEquals(3.14f, 3.14.toPropertyValue(Float::class))
    @Test
    fun `toPropertyValue Double`() = assertEquals(3.14, 3.14.toPropertyValue(Double::class))
    @Test
    fun `toPropertyValue BigDecimal`() = assertEquals(BigDecimal("3.14"), 3.14.toPropertyValue(BigDecimal::class))

    @Test
    fun `toPropertyValue unsupported type throws`() {
        assertFailsWith<IllegalStateException> { 1.0.toPropertyValue(String::class) }
    }

    // ==================== toDoubleValue ====================

    @Test
    fun `toDoubleValue null returns null`() = assertNull(null.toDoubleValue())
    @Test
    fun `toDoubleValue Byte`() = assertEquals(10.0, 10.toByte().toDoubleValue())
    @Test
    fun `toDoubleValue Int`() = assertEquals(42.0, 42.toDoubleValue())
    @Test
    fun `toDoubleValue Double`() = assertEquals(3.14, 3.14.toDoubleValue())
    @Test
    fun `toDoubleValue Long`() = assertEquals(100.0, 100L.toDoubleValue())
    @Test
    fun `toDoubleValue Float`() = assertEquals(2.5, 2.5f.toDoubleValue()!!, 1e-6)
    @Test
    fun `toDoubleValue UInt`() = assertEquals(42.0, 42u.toDoubleValue())
    @Test
    fun `toDoubleValue unsupported throws`() {
        assertFailsWith<IllegalStateException> { "hello".toDoubleValue() }
    }
}
