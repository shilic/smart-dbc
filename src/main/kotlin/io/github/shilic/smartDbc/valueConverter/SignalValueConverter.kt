package io.github.shilic.smartDbc.valueConverter

import io.github.shilic.smartDbc.common.tool.digitsFormat
import io.github.shilic.smartDbc.common.tool.findFirstKeyByValue

/**
 * 查找第一个匹配的键（如果存在）
 * 性能更好，找到第一个匹配项就返回
 */
fun Map<Int, String>.findFirstIndexByValue(value: String): Int? = findFirstKeyByValue(value)

/** 将物理值转换为16进制总线值;
 *
 * 公式: phyValue = hexValue * factor + offset
 *
 * 内部校验精度factor作为除数不可以为0，并抛出异常
 * */
fun Double.phyToHex(factor: Double, offset: Double) : Long = runCatching {
    require(factor != 0.0) {"精度factor作为除数不可以为0, 否则无意义)"}
    ((this - offset) / factor).toLong()
}.getOrElse { exception -> error("将物理值转换为16进制总线值时发生异常 : $exception")}

/** 将16进制总线值转换为物理值
 *
 * 公式: phyValue = hexValue * factor + offset
 *
 * 内部校验精度factor作为除数不可以为0，并抛出异常*/
fun Long.hexToPhy(factor: Double, offset: Double) : Double = runCatching {
    require(factor != 0.0) {"精度factor作为因子不可以为0, 否则无意义)"}
    this * factor + offset
}.getOrElse { exception -> error("将16进制总线值转换为物理值时发生异常 : $exception")}

/* 这里必须要将物理值转换为总线值：因为有时候存在值描述和精度偏移量混用的情况, 例如 '0~100' 表示车速，'0xFF' 表示无效；
   * 故此时, 若需要综合计算值是否在值描述中，必须转换为总线值。
   * 也就是说，值描述中的索引，必须是总线值(忽略精度偏移量)，而不是物理值。 */
/** 将物理值转换为文本值;
 *
 * 综合精度偏移量和值描述进行转换
 * */
fun Double.phyToText(factor: Double, offset: Double, valueTable: Map<Int, String>) : String = when {
    // 没有值描述, 直接返回物理值的字符串形式
    valueTable.count() == 0 -> this.digitsFormat()
    else -> {
        // 获取值描述的索引: 浮点型物理值向下取整至整形; 公式: 物理值 = 原始值 * factor + offset
        val hexValue: Int = this.phyToHex(factor, offset).toInt()
        // 从值描述中取出值 (注意：索引必须是总线值); 如果没有值描述，则返回物理值
        valueTable[hexValue] ?: this.digitsFormat()
    }
}
/** 将文本值转换为物理值 */
fun String.textToPhy(factor: Double, offset: Double, valueTable: Map<Int, String>) : Double = when {
    // 没有值描述, 直接转换为 double 值, 外部自行保留小数点
    valueTable.count() == 0 -> runCatching { this.toDouble() }.getOrElse { e -> error("不存在值描述，并且输入的文本值 '$this' 无法转换为 Double : $e") }
    // 通过值描述直接获取原始的键, 并将总线值的键转换为物理值; 如果值描述查询不到，则抛出异常
    else -> valueTable.findFirstIndexByValue(this)?.toLong()?.hexToPhy(factor, offset) ?: error("在值描述 '$valueTable' 中, 没有查询到值 '$this' 对应的键。")
}

fun Int.hexToText(valueTable: Map<Int, String>): String =  when {
    // 没有值描述, 直接返回值的字符串形式
    valueTable.count() == 0 -> this.toString()
    // 从值描述中取出值 (注意：索引必须是总线值); 如果没有值描述，则直接返回值的字符串形式
    else -> valueTable[this] ?: this.toString()
}
fun String.textToHex(valueTable: Map<Int, String>): Int =  when {
    // 没有值描述, 直接转换为 int 值
    valueTable.count() == 0 -> runCatching { this.toInt() }.getOrElse { e -> error("不存在值描述，并且输入的文本值 '$this' 无法转换为 Int : $e") }
    // 通过值描述直接获取原始的键, 如果值描述查询不到，则抛出异常
    else -> valueTable.findFirstIndexByValue(this) ?: error("在值描述 '$valueTable' 中, 没有查询到值 '$this' 对应的键。")
}

