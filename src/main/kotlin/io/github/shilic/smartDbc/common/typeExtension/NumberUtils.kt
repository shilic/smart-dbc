package io.github.shilic.smartDbc.common.typeExtension

import java.math.BigDecimal
import kotlin.reflect.KClass

/**
 * 浮点型数值保留指定位数的小数
 */
fun Double.digitsFormat(digits: Int = 2): String = "%.${digits}f".format(this)

/**
 * 将任意数值类型安全转换为传入字段的数据类型，兼容无符号类型
 */
fun Double.toPropertyValue(propertyType: KClass<*>): Comparable<*> = when (propertyType) {
    Byte::class -> toInt().toByte()
    Short::class -> toInt().toShort()
    Int::class -> toInt()
    Long::class ->  toLong()

    UByte::class -> toUInt().toUByte()
    UShort::class -> toUInt().toUShort()
    UInt::class -> toUInt()
    ULong::class -> toULong()

    Float::class -> toFloat()
    Double::class -> toDouble()

    BigDecimal::class -> toBigDecimal()
    else -> error("属性类型出错，数据类型必须是 Byte,Short,Int,Long,UByte,UShort,UInt,ULong,Double,Float,BigDecimal 类型;" +
            "不支持的字段类型为: $propertyType")
}

/** 将任意数值类型安全转换为 Double?，兼容无符号类型 */
fun Any?.toDoubleValue(): Double? = when (this) {
    null -> null
    is Byte -> toDouble()
    is Short -> toDouble()
    is Int -> toDouble()
    is Long -> toDouble()

    is UByte -> toDouble()
    is UShort -> toDouble()
    is UInt -> toDouble()
    is ULong -> toDouble()

    is Double -> this
    is Float -> toDouble()

    is BigDecimal -> toDouble()
    is Number -> toDouble()
    else -> error("属性类型出错，数据类型必须是 Byte,Short,Int,Long,UByte,UShort,UInt,ULong,Double,Float,BigDecimal 类型; " +
            "不支持的类型为: ${this::class.simpleName}")
}