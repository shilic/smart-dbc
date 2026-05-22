package tool

import dataModel.dataEnums.CANByteOrder

/** 转换数据格式：Intel(低位存低位) / Motorola(高位存低位) */
enum class DataType { Intel, Motorola }
// ==================== 大转小 =====================

/** 传入 Int，得到不定长的 bit 数组。例如 15 -> {1,1,1,1} */
fun Int.toBits(type: DataType = DataType.Intel): ByteArray {
    var a = this; var len = 0
    for (i in 30 downTo 0) {
        a = a shl 1
        if (a and 0x8000_0000.toInt() == 0x8000_0000.toInt()) { len = i + 1; break }
    }
    return toBits(type, len)
}

/** 传入 Int，得到定长的 bit 数组。 例如2 长度5 ，输出  [0, 1, 0, 0, 0] */
fun Int.toBits(type: DataType = DataType.Intel, bitLength: Int): ByteArray {
    var v = this
    return ByteArray(bitLength) { i ->
        when (type) {
            DataType.Intel -> {
                val b = (v and 1).toByte();
                v = v shr 1;
                b
            }
            DataType.Motorola -> (this shr (bitLength - 1 - i) and 1).toByte()
        }
    }
}

/** Int 转 32 位 bits（定长） */
fun Int.to32Bits(type: DataType = DataType.Intel): ByteArray {
    var v = this
    return ByteArray(32) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 1).toByte(); v = v shr 1; b }
            DataType.Motorola -> (this shr (31 - i) and 1).toByte()
        }
    }
}

/** Int 转 4 位 Int 数组 */
fun Int.toBytesI(type: DataType = DataType.Intel): IntArray {
    var v = this
    return IntArray(4) { i ->
        when (type) {
            DataType.Intel -> { val b = v and 0xFF; v = v shr 8; b }
            DataType.Motorola -> (this shr ((3 - i) * 8)) and 0xFF
        }
    }
}

/** Int 转 4 位 Byte 数组 */
fun Int.to4Bytes(type: DataType = DataType.Intel): ByteArray {
    var v = this
    return ByteArray(4) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 0xFF).toByte(); v = v shr 8; b }
            DataType.Motorola -> (this shr ((3 - i) * 8) and 0xFF).toByte()
        }
    }
}

/** Int 转不定长 Byte 数组 */
fun Int.toBytes(type: DataType = DataType.Intel, length: Int = 4): ByteArray {
    var v = this
    return ByteArray(length) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 0xFF).toByte(); v = v shr 8; b }
            DataType.Motorola -> (this shr ((length - 1 - i) * 8) and 0xFF).toByte()
        }
    }
}

/** Long 转不定长 Byte 数组 */
fun Long.toBytes(type: DataType = DataType.Intel, length: Int): ByteArray {
    var v = this
    return ByteArray(length) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 0xFF).toByte(); v = v shr 8; b }
            DataType.Motorola -> (this shr ((length - 1 - i) * 8) and 0xFF).toByte()
        }
    }
}

/** 计算 Long 数值的有效字节数（去除高位零后） */
fun Long.effectiveByteCount(): Int {
    var v = this; var count = 0
    while (v != 0L) { count++; v = v shr 8 }
    return count
}

// ======================================= byte -> bits ======================================

/** 将 Byte 转换为长度为 8 的 bit 数组 */
fun Byte.to8Bits(type: DataType = DataType.Intel): ByteArray {
    var v = toInt() and 0xFF
    return ByteArray(8) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 1).toByte(); v = v shr 1; b }
            DataType.Motorola -> (toInt() and 0xFF shr (7 - i) and 1).toByte()
        }
    }
}

/** 将 Byte 转换为指定长度的 bit 数组 */
fun Byte.toBits(type: DataType = DataType.Intel, length: Int): ByteArray {
    var v = toInt() and 0xFF
    return ByteArray(length) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 1).toByte(); v = v shr 1; b }
            DataType.Motorola -> (toInt() and 0xFF shr (length - 1 - i) and 1).toByte()
        }
    }
}

// ===================================== bytes -> bits =======================================

/** 将任意长度 Byte 数组转换为 bits 数组。bitLength=-1 表示自动计算 */
fun ByteArray?.toBitsSafe(type: DataType = DataType.Intel, bitLength: Int = -1): ByteArray {
    if (this == null) return ByteArray(if (bitLength > 0) bitLength else 1)
    val actualLen = if (bitLength == -1) size * 8 else bitLength
    val bits = ByteArray(actualLen)
    var currentIndex = 0
    for (b in this) {
        val bits8 = b.to8Bits(type)
        val copyLen = minOf(actualLen - currentIndex, 8)
        bits8.copyInto(bits, currentIndex, 0, copyLen)
        currentIndex += copyLen
        if (currentIndex >= actualLen) break
    }
    return bits
}

/** 长度4的 Byte 数组转 32 位 bit 数组 */
fun ByteArray.from4BytesTo32Bits(type: DataType = DataType.Intel): ByteArray {
    val temp = from4BytesToLong(type)
    var v = temp
    return ByteArray(32) { i ->
        when (type) {
            DataType.Intel -> { val b = (v and 1).toByte(); v = v shr 1; b }
            DataType.Motorola -> (temp shr (31 - i) and 1).toByte()
        }
    }
}

/** 长度8的 Byte 数组转 64 个 bit 数组 */
fun ByteArray.from8BytesTo64Bits(type: DataType = DataType.Intel): ByteArray {
    val bits = ByteArray(64)
    for (i in 0 until 8) this[i].to8Bits(type).copyInto(bits, i * 8)
    return bits
}

// ============================================ 小转大 =========================================

/** 任意长度(<=32) bits 转 Int */
fun ByteArray.bitsToInt(type: DataType = DataType.Intel): Int {
    var re = 0
    when (type) {
        DataType.Intel -> for (i in lastIndex downTo 0) re = (re shl 1) or (this[i].toInt() and 1)
        DataType.Motorola -> for (b in this) re = (re shl 1) or (b.toInt() and 1)
    }
    return re
}

/** 8 bits -> Int 表示的 byte */
fun ByteArray.from8bitsToInt(type: DataType = DataType.Intel): Int {
    var b = 0
    when (type) {
        DataType.Intel -> for (i in 7 downTo 0) b = (b shl 1) or this[i].toInt()
        DataType.Motorola -> for (i in 0 until 8) b = (b shl 1) or this[i].toInt()
    }
    return b
}

/** 8 bits 转 Byte */
fun ByteArray.from8bitsToByte(type: DataType = DataType.Intel): Byte {
    var b = 0
    when (type) {
        DataType.Intel -> for (i in 7 downTo 0) b = (b shl 1) or this[i].toInt()
        DataType.Motorola -> for (i in 0 until 8) b = (b shl 1) or this[i].toInt()
    }
    return b.toByte()
}

/** <8 bits 转 Byte（自动补零到8位） */
fun ByteArray.toBytePadded(type: DataType = DataType.Intel): Byte {
    val padded = ByteArray(8)
    copyInto(padded)
    return padded.from8bitsToByte(type)
}

/** 32 bits -> Int */
fun ByteArray.from32bitsToInt(type: DataType = DataType.Intel): Int {
    var re = 0
    when (type) {
        DataType.Intel -> for (i in 31 downTo 0) re = (re shl 1) or this[i].toInt()
        DataType.Motorola -> for (i in 0 until 32) re = (re shl 1) or this[i].toInt()
    }
    return re
}

/** 64 bits -> 8 位 Int 数组 */
fun ByteArray.from64bitsTo8Ints(type: DataType = DataType.Intel): IntArray =
    IntArray(8) { copyOfRange(it * 8, it * 8 + 8).from8bitsToInt(type) }

/** 64 bits -> 8 位 Byte 数组 */
fun ByteArray.from64bitsTo8Bytes(type: DataType = DataType.Intel): ByteArray =
    ByteArray(8) { copyOfRange(it * 8, it * 8 + 8).from8bitsToByte(type) }

/** bits 转 bytes（不足8的倍数补零） */
fun ByteArray.bitsToBytes(type: DataType = DataType.Intel): ByteArray {
    val pad = (8 - size % 8) % 8
    val padded = if (pad != 0) ByteArray(size + pad).also { copyInto(it) } else copyOf()
    return ByteArray(padded.size / 8) { i ->
        padded.copyOfRange(i * 8, i * 8 + 8).from8bitsToByte(type)
    }
}

// ========================================== bytes -> int/long ===============================

/** 4位byte转Int。例如 0x18,0xFE,0x01,0x1b -> 0x18fe011b */
fun ByteArray.from4BytesToInt(type: DataType = DataType.Intel): Int {
    var ans = 0
    when (type) {
        DataType.Intel -> for (i in 3 downTo 0) ans = (ans shl 8) or (this[i].toInt() and 0xFF)
        DataType.Motorola -> for (i in 0 until 4) ans = (ans shl 8) or (this[i].toInt() and 0xFF)
    }
    return ans
}

/** 任意长度 Byte 数组转 Int */
fun ByteArray.bytesToInt(type: DataType = DataType.Intel): Int {
    var ans = 0
    when (type) {
        DataType.Intel -> for (i in lastIndex downTo 0) ans = (ans shl 8) or this[i].toInt()
        DataType.Motorola -> for (b in this) ans = (ans shl 8) or b.toInt()
    }
    return ans
}

/** 任意长度 Byte 数组转 Long */
fun ByteArray.bytesToLong(type: DataType = DataType.Intel): Long {
    var ans = 0L
    when (type) {
        DataType.Intel -> for (i in lastIndex downTo 0) ans = (ans shl 8) or this[i].toLong()
        DataType.Motorola -> for (b in this) ans = (ans shl 8) or b.toLong()
    }
    return ans
}

/** 4位byte转Long（防负数）。例如 0x18,0xFE,0x01,0x1b -> 0x18fe011b */
fun ByteArray.from4BytesToLong(type: DataType = DataType.Intel): Long {
    var ans = 0L
    when (type) {
        DataType.Intel -> for (i in 3 downTo 0) ans = (ans shl 8) or (this[i].toLong() and 0xFFL)
        DataType.Motorola -> for (i in 0 until 4) ans = (ans shl 8) or (this[i].toLong() and 0xFFL)
    }
    return ans
}

/** 两个 Byte 合成 Int。例如 0xAF,0xFE -> 0xAFFE */
fun Byte.toIntWithLower(lower: Byte): Int =
    ((toInt() and 0xFF) shl 8) or (lower.toInt() and 0xFF)

// ========================================== 打印 / 转换 ======================================

/** Byte 数组转16进制字符串，紧凑格式如 { A0B1C2 } */
fun ByteArray?.toCompactHex(): String {
    if (this == null || isEmpty()) return ""
    return joinToString("") { "%02X".format(it) }.let { "{ $it }" }
}

// ========================================== transOrder =======================================

/** CANByteOrder -> DataType */
fun CANByteOrder.toDataType(): DataType =
    if (this == CANByteOrder.Intel) DataType.Intel else DataType.Motorola

/** DataType -> CANByteOrder */
fun DataType.toCANByteOrder(): CANByteOrder =
    if (this == DataType.Intel) CANByteOrder.Intel else CANByteOrder.MotorolaLSB
