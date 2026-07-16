package valueConverterTest

import io.github.shilic.numberUtils.toBits
import io.github.shilic.smartDbc.can.models.canFrame.models.CanFrameData
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.models.*
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import io.github.shilic.smartDbc.valueConverter.*
import demoData.*
import java.io.File
import kotlin.test.Test
import kotlin.test.*

class MessageValueConverterTest {

    private fun readExampleDbc(): DataBaseCanImp = DbcFileReader(ExampleDbcPath1).read()

    // ==================== intelBitsToHex ====================

    @Test
    fun `intelBitsToHex single byte`() {
        val bits = byteArrayOf(1, 1, 1, 1, 0, 0, 0, 0) // 0xF0 → 240 in decimal
        // Actually: bits[0]=1, bits[1]=1, bits[2]=1, bits[3]=1 → 0x0F = 15
        // Wait, intelBitsToHex uses copyOfRange(startBit, startBit+bitLength).bitsToLong()
        // copyOfRange on ByteArray copies elements, not bits
        val fullBits = ByteArray(64) { 0 }
        System.arraycopy(bits, 0, fullBits, 0, 8)
        val result = fullBits.intelBitsToHex(0, 8)
        assertEquals(15, result) // 1111_0000 → bitsToLong treats it LSB first: bits[0] as LSB
    }

    @Test
    fun `intelBitsToHex cross byte`() {
        val bits = ByteArray(64) { 0 }
        for (i in 0 until 16) bits[i] = 1 // all 1s for 16 bits
        val result = bits.intelBitsToHex(0, 16)
        assertEquals(0xFFFF, result)
    }

    // ==================== intelBitsToPhy ====================

    @Test
    fun `intelBitsToPhy basic`() {
        val bits = ByteArray(64) { 0 }
        bits[0] = 1; bits[1] = 0; bits[2] = 1; bits[3] = 0 // 0101 = 5
        // bitsToLong: bits[0]=1 is LSB → 1 + 0*2 + 1*4 + 0*8 = 5
        val result = bits.intelBitsToPhy(0, 4, 1.0, 0.0)
        assertEquals(5.0, result)
    }

    @Test
    fun `intelBitsToPhy with factor and offset`() {
        val bits = ByteArray(64) { 0 }
        bits[0] = 1 // hex=1, phy=1*2+10=12
        val result = bits.intelBitsToPhy(0, 1, 2.0, 10.0)
        assertEquals(12.0, result)
    }

    // ==================== decodeCanFrame / encodeCanFrame (DBC level) ====================

    @Test
    fun `decodeCanFrame DBC level decodes all 8 signals correctly`() {
        val dbc = readExampleDbc()
        val frame = CanFrameData(msg1_Id, data8_1)
        dbc.decodeCanFrame(frame)
        val msg = dbc[msg1_Id] ?: fail("message not found")

        assertEquals(30.0,  msg["msg1_sig1"]?.currentPhyValue ?: fail("sig1"))
        assertEquals(29.0,  msg["msg1_sig2"]?.currentPhyValue ?: fail("sig2"))
        assertEquals(28.0,  msg["msg1_sig3"]?.currentPhyValue ?: fail("sig3"))
        assertEquals(20.0,  msg["msg1_sig4"]?.currentPhyValue ?: fail("sig4"))
        assertEquals(22.2,  msg["msg1_sig5"]?.currentPhyValue ?: fail("sig5"), 1e-6)
        assertEquals(10.5,  msg["msg1_sig6"]?.currentPhyValue ?: fail("sig6"), 1e-6)
        assertEquals(-80.5, msg["msg1_sig7"]?.currentPhyValue ?: fail("sig7"), 1e-6)
        assertEquals(110.0, msg["msg1_sig8"]?.currentPhyValue ?: fail("sig8"), 1e-6)

        // Verify hex/bus values as well
        assertEquals(30L,   msg["msg1_sig1"]?.currentHexValue)
        assertEquals(211L,  msg["msg1_sig5"]?.currentHexValue)
    }

    @Test
    fun `encodeCanFrame round-trip preserves all 8 bytes`() {
        val dbc = readExampleDbc()
        dbc.decodeCanFrame(CanFrameData(msg1_Id, data8_1))

        // Modify two signals
        dbc[msg1_Id, "msg1_sig1"]?.currentPhyValue = 42.0
        dbc[msg1_Id, "msg1_sig2"]?.currentPhyValue = 16.0

        val encoded = dbc.encodeCanFrame(msg1_Id)
        // sig1 at byte0 → 42, sig2 at byte1 → 16, rest unchanged from data8_1
        assertEquals(42.toByte(),              encoded.data[0])
        assertEquals(16.toByte(),              encoded.data[1])
        assertEquals(data8_1[2],               encoded.data[2])
        assertEquals(data8_1[3],               encoded.data[3])
        assertEquals(data8_1[4],               encoded.data[4])
        assertEquals(data8_1[5],               encoded.data[5])
        assertEquals(data8_1[6],               encoded.data[6])
        assertEquals(data8_1[7],               encoded.data[7])
    }

    // ==================== decodeBytes / encodeBytes (DBC level) ====================

    @Test
    fun `decodeBytes DBC level decodes all 8 signals`() {
        val dbc = readExampleDbc()
        dbc.decodeBytes(msg1_Id, data8_1)
        val msg = dbc[msg1_Id] ?: fail("message not found")

        assertEquals(30.0,  msg["msg1_sig1"]?.currentPhyValue ?: fail("sig1"))
        assertEquals(29.0,  msg["msg1_sig2"]?.currentPhyValue ?: fail("sig2"))
        assertEquals(28.0,  msg["msg1_sig3"]?.currentPhyValue ?: fail("sig3"))
        assertEquals(20.0,  msg["msg1_sig4"]?.currentPhyValue ?: fail("sig4"))
        assertEquals(22.2,  msg["msg1_sig5"]?.currentPhyValue ?: fail("sig5"), 1e-6)
        assertEquals(10.5,  msg["msg1_sig6"]?.currentPhyValue ?: fail("sig6"), 1e-6)
        assertEquals(-80.5, msg["msg1_sig7"]?.currentPhyValue ?: fail("sig7"), 1e-6)
        assertEquals(110.0, msg["msg1_sig8"]?.currentPhyValue ?: fail("sig8"), 1e-6)
    }

    @Test
    fun `encodeBytes DBC level produces correct byte array`() {
        val dbc = readExampleDbc()
        // Set all signal values to known numbers
        dbc[msg1_Id, "msg1_sig1"]?.currentPhyValue = 30.0
        dbc[msg1_Id, "msg1_sig2"]?.currentPhyValue = 29.0
        dbc[msg1_Id, "msg1_sig3"]?.currentPhyValue = 28.0
        dbc[msg1_Id, "msg1_sig4"]?.currentPhyValue = 20.0
        dbc[msg1_Id, "msg1_sig5"]?.currentPhyValue = 22.2
        dbc[msg1_Id, "msg1_sig6"]?.currentPhyValue = 10.5
        dbc[msg1_Id, "msg1_sig7"]?.currentPhyValue = -80.5
        dbc[msg1_Id, "msg1_sig8"]?.currentPhyValue = 110.0

        val bytes = dbc.encodeBytes(msg1_Id)
        assertTrue(data8_1.contentEquals(bytes), "encoded bytes should match original data8_1")
    }

    // ==================== decodeBytes / encodeBytes (Message level) ====================

    @Test
    fun `decodeBytes message level decodes all 8 signals`() {
        val dbc = readExampleDbc()
        val msg = dbc[msg1_Id] ?: fail("message not found")
        msg.decodeBytes(data8_1)
        assertEquals(30.0,  msg["msg1_sig1"]?.currentPhyValue ?: fail("sig1"))
        assertEquals(29.0,  msg["msg1_sig2"]?.currentPhyValue ?: fail("sig2"))
        assertEquals(28.0,  msg["msg1_sig3"]?.currentPhyValue ?: fail("sig3"))
        assertEquals(20.0,  msg["msg1_sig4"]?.currentPhyValue ?: fail("sig4"))
        assertEquals(22.2,  msg["msg1_sig5"]?.currentPhyValue ?: fail("sig5"), 1e-6)
        assertEquals(10.5,  msg["msg1_sig6"]?.currentPhyValue ?: fail("sig6"), 1e-6)
        assertEquals(-80.5, msg["msg1_sig7"]?.currentPhyValue ?: fail("sig7"), 1e-6)
        assertEquals(110.0, msg["msg1_sig8"]?.currentPhyValue ?: fail("sig8"), 1e-6)
    }

    @Test
    fun `encodeBytes message level round-trip decodes back correctly`() {
        val dbc = readExampleDbc()
        val msg = dbc[msg1_Id] ?: fail("message not found")
        // Decode known data
        msg.decodeBytes(data8_1)
        // Re-encode
        val bytes = msg.encodeBytes()
        // Decode again into fresh DBC
        val dbc2 = readExampleDbc()
        dbc2.decodeBytes(msg1_Id, bytes)
        assertEquals(30.0,  dbc2[msg1_Id, "msg1_sig1"]?.currentPhyValue ?: fail("sig1"))
        assertEquals(29.0,  dbc2[msg1_Id, "msg1_sig2"]?.currentPhyValue ?: fail("sig2"))
        assertEquals(22.2,  dbc2[msg1_Id, "msg1_sig5"]?.currentPhyValue ?: fail("sig5"), 1e-6)
        assertEquals(-80.5, dbc2[msg1_Id, "msg1_sig7"]?.currentPhyValue ?: fail("sig7"), 1e-6)
        assertEquals(110.0, dbc2[msg1_Id, "msg1_sig8"]?.currentPhyValue ?: fail("sig8"), 1e-6)
    }

    // ==================== bitsToHexValue ====================

    @Test
    fun `bitsToHexValue Intel`() {
        // Create a bits array: bit0=1, bit1=0, bit2=1 → 1 + 0 + 4 = 5
        val bits = ByteArray(64) { 0 }
        bits[0] = 1; bits[1] = 0; bits[2] = 1
        val result = bits.bitsToHexValue(CanByteOrder.Intel, 0, 3)
        assertEquals(5, result)
    }

    @Test
    fun `bitsToHexValue MotorolaMSB`() {
        // Create bits for byte0=0x42: bit7=0,bit6=1,bit5=0,bit4=0,bit3=0,bit2=0,bit1=1,bit0=0
        val data = byteArrayOf(0x42.toByte(), 0, 0, 0, 0, 0, 0, 0)
        val bits = data.toBits()
        val result = bits.bitsToHexValue(CanByteOrder.MotorolaMSB, 7, 8)
        assertEquals(0x42, result)
    }

    // ==================== motorolaIntoBits ====================

    @Test
    fun `motorolaIntoBits MotorolaMSB basic`() {
        val matrix = ByteArray(64) { 0 }
        val sigBits = byteArrayOf(1, 0, 1, 0) // LSB first: 0101 = 5
        motorolaIntoBits(matrix, sigBits, 7, 4, CanByteOrder.MotorolaMSB)
        // Verify bits were placed
        val result = matrix.bitsToHexValue(CanByteOrder.MotorolaMSB, 7, 4)
        assertEquals(5, result)
    }

    @Test
    fun `motorolaIntoBits empty matrix throws`() {
        assertFailsWith<IllegalArgumentException> {
            motorolaIntoBits(ByteArray(0), byteArrayOf(1), 0, 1, CanByteOrder.MotorolaMSB)
        }
    }

    @Test
    fun `motorolaIntoBits zero bitLength throws`() {
        assertFailsWith<IllegalArgumentException> {
            motorolaIntoBits(ByteArray(64), byteArrayOf(1), 0, 0, CanByteOrder.MotorolaMSB)
        }
    }

    @Test
    fun `motorolaIntoBits Intel byte order throws`() {
        assertFailsWith<IllegalStateException> {
            motorolaIntoBits(ByteArray(64), byteArrayOf(1), 0, 1, CanByteOrder.Intel)
        }
    }

    // ==================== encodeBytes round-trip Motorola signal ====================

    @Test
    fun `encodeBytes for message with Motorola signals does not throw`() {
        val dbc = readExampleDbc()
        // motorMsg1: 2560163839 → msgId = 0x1899_5447
        val motorMsgId = 0x1899_5447
        val msg = dbc.msgMap.values.firstOrNull { it.msgName == "motorMsg1" } ?: fail("motorMsg1 not found")
        msg.signalMap.values.forEach { sig -> sig.currentPhyValue = 0.0 }
        assertTrue(msg.signalMap.isNotEmpty(), "motorMsg1 should have signals")
        // encode should not throw for Motorola signals
        val bytes = msg.encodeBytes()
        assertTrue(bytes.isNotEmpty())
        // decode back should not throw
        msg.decodeBytes(bytes)
    }
}
