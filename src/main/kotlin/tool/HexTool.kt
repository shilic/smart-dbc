package tool

import kotlin.collections.isEmpty
import kotlin.collections.joinToString

// ============================== 数组 → 十六进制 ==============================
/** ByteArray -> "{ 0xAB, 0xCD }" 格式 */
fun ByteArray?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%02X".format(it) }
}
/** ShortArray -> "{ 0x0001, 0x0002 }" 格式 */
fun ShortArray?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%04X".format(it) }
}
/** IntArray -> "{ 0x00000001, 0x00000002 }" 格式 */
fun IntArray?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%08X".format(it) }
}
/** LongArray -> 16进制格式 */
fun LongArray?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%16X".format(it) }
}

//  ============================== 集合 → 十六进制 ==============================
/** List Byte -> 16进制格式 */
fun List<Byte>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%02X".format(it) }
}

//  ============================== 泛型数组 → 十六进制 ==============================
/** Array Number -> 16进制格式 */
fun Array<Number>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { it.toHexStr()  }
}
/** Array UByte -> 16进制格式 */
fun Array<UByte>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%02X".format(it)  }
}
/** Array UShort -> 16进制格式 */
fun Array<UShort>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%04X".format(it)  }
}
/** Array UInt -> 16进制格式 */
fun Array<UInt>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%08X".format(it)  }
}
/** Array ULong -> 16进制格式 */
fun Array<ULong>?.toHexStr(): String {
    if (this == null || isEmpty()) return "{}"
    return joinToString(", ", "{ ", " }") { "0x%16X".format(it)  }
}
// ============================== 紧凑格式 ==============================
/** ByteArray -> 紧凑格式 "AB01FE"（无0x前缀，无分隔符） */
fun ByteArray?.toCompactHexStr(): String {
    if (this == null || isEmpty()) return ""
    return joinToString("") { "%02X".format(it) }
}
/** Array Byte -> 紧凑格式 "AB01FE"（无0x前缀，无分隔符） */
fun Array<Byte>?.toCompactHexStr(): String {
    if (this == null || isEmpty()) return ""
    return joinToString("") { "%02X".format(it) }
}
/** Array UByte -> 紧凑格式 "AB01FE"（无0x前缀，无分隔符） */
fun Array<UByte>?.toCompactHexStr(): String {
    if (this == null || isEmpty()) return ""
    return joinToString("") { "%02X".format(it) }
}
// ============================== 单值 → 十六进制 ==============================
/** 数值 → 16进制格式 */
fun Number.toHexStr(): String = when (this) {
    is Byte -> toByte().toHexStr()
    is Short -> toShort().toHexStr()
    is Int -> toInt().toHexStr()
    is Long -> toLong().toHexStr()
    is Float, Double -> throw IllegalArgumentException("扩展函数 ${::toHexStr.name} 只支持将整形的数值转换为16进制字符串，不支持浮点型 ")
    else -> throw IllegalArgumentException("扩展函数 ${::toHexStr.name} 只支持将整形的数值转换为16进制字符串，不支持未知的 ${Number::class.simpleName} 类型 ")
}

/** Byte -> 16进制格式 */
fun Byte.toHexStr(): String = "0x%02X".format(this)
/** Short -> 16进制格式 */
fun Short.toHexStr(): String = "0x%04X".format(this)
/** Int -> 16进制格式 */
fun Int.toHexStr(): String = "0x%08X".format(this)
/** Long -> 16进制格式 */
fun Long.toHexStr(): String = "0x%16X".format(this)

/** UByte -> 16进制格式 */
fun UByte.toHexStr(): String = "0x%02X".format(this)
/** UShort -> 16进制格式 */
fun UShort.toHexStr(): String = "0x%04X".format(this)
/** UInt -> 16进制格式 */
fun UInt.toHexStr(): String = "0x%08X".format(this)
/** ULong -> 16进制格式 */
fun ULong.toHexStr(): String = "0x%16X".format(this)

/** Long -> 8位十六进制（截断高32位） */
fun Long.toHex8(): String = "0x%08X".format(this.toInt())
/** Int -> 8位十六进制 */
fun Int.toHex8(): String = "0x%08X".format(this)
