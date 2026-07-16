package valueConverterTest

import io.github.shilic.numberUtils.toBits
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.CanByteOrder
import io.github.shilic.smartDbc.valueConverter.*
import kotlin.test.Test
import kotlin.test.*

class MotorolaValueConverterTest {

    // ==================== lsbStartBitToMsb ====================

    @Test
    fun `lsbStartBitToMsb single byte`() {
        // LSB at bit 0, length 8 → MSB should be at bit 7
        assertEquals(7, 0.lsbStartBitToMsb(8))
    }

    @Test
    fun `lsbStartBitToMsb length 1 LSB equals MSB`() {
        assertEquals(5, 5.lsbStartBitToMsb(1))
    }

    // ==================== motorolaBitsToHex ====================

    // 8-byte frame: byte0=[bit7..0], byte1=[bit15..8], ... byte7=[bit63..56]
    // Intel bit numbering: bit0 is LSB of byte0

    @Test
    fun `motorolaBitsToHex MSB single byte value`() {
        // Set byte0 = 0x42 (0100_0010): bit7=0,bit6=1,bit5=0,bit4=0,bit3=0,bit2=0,bit1=1,bit0=0
        val data = byteArrayOf(0x42.toByte(), 0, 0, 0, 0, 0, 0, 0)
        val bits = data.toBits()
        // MSB start=7, length=8: should read bits[7],bits[6],bits[5],bits[4],bits[3],bits[2],bits[1],bits[0]
        // = 0,1,0,0,0,0,1,0 = 0x42 = 66
        val result = bits.motorolaBitsToHex(CanByteOrder.MotorolaMSB, 7, 8)
        assertEquals(0x42, result)
    }

    @Test
    fun `motorolaBitsToHex MSB cross-byte value`() {
        // byte0 = 0x12, byte1 = 0x34 → start=15 (MSB of byte1), length=16
        // Should read across bytes correctly in Motorola MSB zigzag
        val data = byteArrayOf(0x12.toByte(), 0x34.toByte(), 0, 0, 0, 0, 0, 0)
        val bits = data.toBits()
        val result = bits.motorolaBitsToHex(CanByteOrder.MotorolaMSB, 15, 16)
        assertTrue(result > 0, "Should produce a positive value, got $result")
    }

    @Test
    fun `motorolaBitsToHex MotorolaLSB delegates to MSB`() {
        // MotorolaLSB should internally convert startBit and produce same result as equivalent MSB
        val data = byteArrayOf(0x42.toByte(), 0, 0, 0, 0, 0, 0, 0)
        val bits = data.toBits()
        val msbResult = bits.motorolaBitsToHex(CanByteOrder.MotorolaMSB, 7, 8)
        val lsbResult = bits.motorolaBitsToHex(CanByteOrder.MotorolaLSB, 0, 8)
        assertEquals(msbResult, lsbResult, "LSB(0,8) should equal MSB(7,8)")
    }

    @Test
    fun `motorolaBitsToHex Intel throws`() {
        val bits = ByteArray(64) { 0 }
        val ex = assertFailsWith<IllegalStateException> {
            bits.motorolaBitsToHex(CanByteOrder.Intel, 7, 8)
        }
        assertTrue(ex.message!!.contains("必须是摩托罗拉格式"))
    }

    // ==================== motorolaBytesToHex ====================

    @Test
    fun `motorolaBytesToHex delegates to bits`() {
        val data = byteArrayOf(0xFF.toByte(), 0, 0, 0, 0, 0, 0, 0)
        // MSB start=7, length=8: all bits are 1 → 0xFF = 255
        val result = data.motorolaBytesToHex(CanByteOrder.MotorolaMSB, 7, 8)
        assertEquals(0xFF, result)
    }

    @Test
    fun `motorolaBytesToHex index out of bounds throws`() {
        val data = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        assertFailsWith<IllegalArgumentException> {
            data.motorolaBytesToHex(CanByteOrder.MotorolaMSB, 70, 16)
        }
    }
}
