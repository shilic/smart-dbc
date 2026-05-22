package tool

import kotlin.test.Test
import kotlin.test.assertEquals

class HexToolTest {

    // ==================== 数组 -> hex 字符串 ====================

    @Test fun `ByteArray toHexStr valid`() =
        assertEquals("{ 0xAB, 0xCD }", byteArrayOf(0xAB.toByte(), 0xCD.toByte()).toHexStr(),
            "ByteArray 应输出带 0x 前缀的16进制格式")

    @Test fun `ByteArray toHexStr single`() =
        assertEquals("{ 0x00 }", byteArrayOf(0).toHexStr(),
            "单个元素的 ByteArray 应输出单个16进制值")

    @Test fun `ByteArray toHexStr null`() {
        val arr: ByteArray? = null
        assertEquals("{}", arr.toHexStr(), "null ByteArray 应返回空花括号")
    }

    @Test fun `ByteArray toHexStr empty`() =
        assertEquals("{}", byteArrayOf().toHexStr(),
            "空 ByteArray 应返回空花括号")

    @Test fun `ShortArray toHexStr valid`() =
        assertEquals("{ 0x0001, 0x0002 }", shortArrayOf(1, 2).toHexStr(),
            "ShortArray 应输出 4 位16进制格式")

    @Test fun `ShortArray toHexStr null`() {
        val arr: ShortArray? = null
        assertEquals("{}", arr.toHexStr(), "null ShortArray 应返回空花括号")
    }

    @Test fun `ShortArray toHexStr empty`() =
        assertEquals("{}", shortArrayOf().toHexStr(),
            "空 ShortArray 应返回空花括号")

    @Test fun `IntArray toHexStr valid`() =
        assertEquals("{ 0x00000001, 0x00000ABC }", intArrayOf(1, 0xABC).toHexStr(),
            "IntArray 应输出 8 位16进制格式")

    @Test fun `IntArray toHexStr null`() {
        val arr: IntArray? = null
        assertEquals("{}", arr.toHexStr(), "null IntArray 应返回空花括号")
    }

    @Test fun `IntArray toHexStr empty`() =
        assertEquals("{}", intArrayOf().toHexStr(),
            "空 IntArray 应返回空花括号")

    @Test fun `LongArray toHexStr valid`() =
        assertEquals("{ 0x0000000000000001, 0x1BCDEF0123456789 }",
            longArrayOf(1, 0x1BCDEF0123456789L).toHexStr(),
            "LongArray 应输出 16 位16进制格式")

    @Test fun `LongArray toHexStr null`() {
        val arr: LongArray? = null
        assertEquals("{}", arr.toHexStr(), "null LongArray 应返回空花括号")
    }

    @Test fun `LongArray toHexStr empty`() =
        assertEquals("{}", longArrayOf().toHexStr(),
            "空 LongArray 应返回空花括号")

    // ==================== List -> hex ====================

    @Test fun `List Byte toHexStr valid`() =
        assertEquals("{ 0x01, 0xFF }", listOf<Byte>(1, -1).toHexStr(),
            "List<Byte> 应输出带 0x 前缀的16进制格式")

    @Test fun `List Byte toHexStr null`() {
        val list: List<Byte>? = null
        assertEquals("{}", list.toHexStr(), "null List 应返回空花括号")
    }

    @Test fun `List Byte toHexStr empty`() =
        assertEquals("{}", listOf<Byte>().toHexStr(),
            "空 List 应返回空花括号")

    // ==================== Array<Number> -> hex ====================

    @Test fun `Array Number toHexStr mixed`() {
        val arr: Array<Number> = arrayOf(1.toByte(), 2.toShort(), 3, 4L)
        assertEquals("{ 0x01, 0x0002, 0x00000003, 0x0000000000000004 }", arr.toHexStr(),
            "Array<Number> 中每种类型应按各自的位宽输出16进制")
    }

    @Test fun `Array Number toHexStr null`() {
        val arr: Array<Number>? = null
        assertEquals("{}", arr.toHexStr(), "null Array<Number> 应返回空花括号")
    }

    @Test fun `Array Number toHexStr empty`() =
        assertEquals("{}", arrayOf<Number>().toHexStr(),
            "空 Array<Number> 应返回空花括号")

    // ==================== Array<unsigned> -> hex ====================

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `Array UByte toHexStr valid`() {
        assertEquals("{ 0x01, 0xFF }", arrayOf<UByte>(1u, 255u).toHexStr(),
            "Array<UByte> 应输出 2 位16进制格式")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `Array UByte toHexStr null`() {
        val arr: Array<UByte>? = null
        assertEquals("{}", arr.toHexStr(), "null Array<UByte> 应返回空花括号")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `Array UShort toHexStr valid`() {
        assertEquals("{ 0x0001, 0xFFFF }", arrayOf<UShort>(1u, 65535u).toHexStr(),
            "Array<UShort> 应输出 4 位16进制格式")
    }
    @Test
    fun `Array UShort toHexStr null`() {
        val arr: Array<UShort>? = null
        assertEquals("{}", arr.toHexStr(), "null Array<UShort> 应返回空花括号")
    }
    @Test
    fun `Array UInt toHexStr valid`() {
        assertEquals("{ 0x00000001, 0xFFFFFFFF }", arrayOf<UInt>(1u, 0xFFFFFFFFu).toHexStr(),
            "Array<UInt> 应输出 8 位16进制格式")
    }
    @Test
    fun `Array UInt toHexStr null`() {
        val arr: Array<UInt>? = null
        assertEquals("{}", arr.toHexStr(), "null Array<UInt> 应返回空花括号")
    }
    @Test
    fun `Array ULong toHexStr valid`() {
        assertEquals("{ 0x0000000000000001, 0xFFFFFFFFFFFFFFFF }",
            arrayOf<ULong>(1uL, 0xFFFFFFFFFFFFFFFFuL).toHexStr(),
            "Array<ULong> 应输出 16 位16进制格式")
    }
    @Test
    fun `Array ULong toHexStr null`() {
        val arr: Array<ULong>? = null
        assertEquals("{}", arr.toHexStr(), "null Array<ULong> 应返回空花括号")
    }

    // ==================== 紧凑格式 ====================

    @Test fun `ByteArray toCompactHexStr valid`() =
        assertEquals("AB01FE", byteArrayOf(0xAB.toByte(), 0x01.toByte(), 0xFE.toByte()).toCompactHexStr(),
            "ByteArray.toCompactHexStr 应输出无前缀无分隔符的紧凑格式")

    @Test fun `ByteArray toCompactHexStr null`() {
        val arr: ByteArray? = null
        assertEquals("", arr.toCompactHexStr(), "null ByteArray 紧凑格式应返回空字符串")
    }

    @Test fun `ByteArray toCompactHexStr empty`() =
        assertEquals("", byteArrayOf().toCompactHexStr(),
            "空 ByteArray 紧凑格式应返回空字符串")

    @Test fun `Array Byte toCompactHexStr valid`() =
        assertEquals("AB01FE", arrayOf<Byte>(0xAB.toByte(), 0x01.toByte(), 0xFE.toByte()).toCompactHexStr(),
            "Array<Byte>.toCompactHexStr 应输出无前缀无分隔符的紧凑格式")

    @Test fun `Array Byte toCompactHexStr null`() {
        val arr: Array<Byte>? = null
        assertEquals("", arr.toCompactHexStr(), "null Array<Byte> 紧凑格式应返回空字符串")
    }
    @Test
    fun `Array UByte toCompactHexStr valid`() {
        assertEquals("AB01FE", arrayOf<UByte>(0xABu.toUByte(), 0x01u.toUByte(), 0xFEu.toUByte()).toCompactHexStr(),
            "Array<UByte>.toCompactHexStr 应输出无前缀无分隔符的紧凑格式")
    }
    @Test
    fun `Array UByte toCompactHexStr null`() {
        val arr: Array<UByte>? = null
        assertEquals("", arr.toCompactHexStr(), "null Array<UByte> 紧凑格式应返回空字符串")
    }

    // ==================== 单值 -> hex ====================

    @Test fun `Byte toHexStr`() =
        assertEquals("0x01", 1.toByte().toHexStr(), "Byte.toHexStr 应输出 2 位16进制")
    @Test fun `Byte toHexStr zero`() =
        assertEquals("0x00", 0.toByte().toHexStr(), "Byte 值为 0 应输出 0x00")
    @Test fun `Byte toHexStr negative`() =
        assertEquals("0xFF", (-1).toByte().toHexStr(), "Byte 负值应输出对应的16进制")
    @Test fun `Short toHexStr`() =
        assertEquals("0x0001", 1.toShort().toHexStr(), "Short.toHexStr 应输出 4 位16进制")
    @Test fun `Short toHexStr max`() =
        assertEquals("0x7FFF", Short.MAX_VALUE.toHexStr(), "Short 最大值应正确格式化")
    @Test fun `Int toHexStr`() =
        assertEquals("0x00000001", 1.toHexStr(), "Int.toHexStr 应输出 8 位16进制")
    @Test fun `Int toHexStr max`() =
        assertEquals("0x7FFFFFFF", Int.MAX_VALUE.toHexStr(), "Int 最大值应正确格式化")
    @Test fun `Long toHexStr`() =
        assertEquals("0x0000000000000001", 1L.toHexStr(), "Long.toHexStr 应输出 16 位16进制")
    @Test fun `Long toHexStr max`() =
        assertEquals("0x7FFFFFFFFFFFFFFF", Long.MAX_VALUE.toHexStr(), "Long 最大值应正确格式化")
    @Test fun `UByte toHexStr`() =
        assertEquals("0x01", 1u.toUByte().toHexStr(), "UByte.toHexStr 应输出 2 位16进制")
    @Test fun `UShort toHexStr`() =
        assertEquals("0x0001", 1u.toUShort().toHexStr(), "UShort.toHexStr 应输出 4 位16进制")
    @Test fun `UInt toHexStr`() =
        assertEquals("0x00000001", 1u.toHexStr(), "UInt.toHexStr 应输出 8 位16进制")
    @Test fun `ULong toHexStr`() =
        assertEquals("0x0000000000000001", 1uL.toHexStr(), "ULong.toHexStr 应输出 16 位16进制")
    @Test fun `ULong toHexStr 2`() =
        assertEquals("0xAFFFFFFFFFFFFFF1", 0xAFFFFFFFFFFFFFF1uL.toHexStr(), "ULong.toHexStr 应输出 16 位16进制")

    // ==================== Number.toHexStr ====================

    @Test fun `Number Byte toHexStr`() {
        val n: Number = 1.toByte()
        assertEquals("0x01", n.toHexStr(), "Number(Byte) 应委托给 Byte.toHexStr")
    }

    @Test fun `Number Short toHexStr`() {
        val n: Number = 1.toShort()
        assertEquals("0x0001", n.toHexStr(), "Number(Short) 应委托给 Short.toHexStr")
    }

    @Test fun `Number Int toHexStr`() {
        val n: Number = 1
        assertEquals("0x00000001", n.toHexStr(), "Number(Int) 应委托给 Int.toHexStr")
    }

    @Test fun `Number Long toHexStr`() {
        val n: Number = 1L
        assertEquals("0x0000000000000001", n.toHexStr(), "Number(Long) 应委托给 Long.toHexStr")
    }

    // ==================== toHex8 ====================

    @Test fun `Long toHex8`() =
        assertEquals("0x00000001", 1L.toHex8(), "Long.toHex8 应截断为 8 位16进制")

    @Test fun `Long toHex8 truncate`() =
        assertEquals("0x89ABCDEF", 0x123456789ABCDEFL.toHex8(),
            "Long.toHex8 应截断高32位，只保留低 8 位16进制")

    @Test fun `Int toHex8`() =
        assertEquals("0x00000001", 1.toHex8(), "Int.toHex8 应输出 8 位16进制")
}
