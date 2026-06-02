package io.github.shilic.smartDbc.valueConverter

import io.github.shilic.numberUtils.bitsToLong
import io.github.shilic.numberUtils.toBits
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.CanByteOrder


/**
 * 主入口函数：通过字节数组和起始位解析信号为总线未处理值。
 * 这是对原始 `parseMotorolaByBytes` 的 Kotlin 化封装。
 *
 * @param this 原始 CAN 数据帧字节数组
 * @param startBit 信号起始位在整帧中的位置 (0-based, 符合 SAE J1939/ISO 11898 等常见约定)
 * @param bitLength 信号位长度
 * @param byteOrder 字节序
 * @return [Long] 返回总线未处理值
 */
fun ByteArray.motorolaBytesToHex(byteOrder: CanByteOrder, startBit: Int, bitLength: Int): Long {
    return toBits().motorolaBitsToHex(byteOrder, startBit, bitLength)
}

/**
 * 核心解析函数：通过比特数组解析信号为总线未处理值。
 *
 * @param this 已转换为比特 (0/1) 的数组
 * @param startBit 信号起始位
 * @param bitLength 信号位长度
 * @param byteOrder 字节序
 * @return [Long] 返回总线未处理值
 */
fun ByteArray.motorolaBitsToHex(byteOrder: CanByteOrder, startBit: Int, bitLength: Int): Long = when (byteOrder) {
    CanByteOrder.MotorolaLSB -> {
        // 计算等效的 MSB 起始位，然后调用 MSB 解析
        val msbStartBit = startBit.lsbStartBitToMsb(bitLength)
        parseMotorolaMsb(msbStartBit, bitLength, this)
    }
    CanByteOrder.MotorolaMSB -> parseMotorolaMsb(startBit, bitLength, this)
    else -> error("必须是摩托罗拉格式，才能被 '${::motorolaBitsToHex.name}' 函数解析")
}

/**
 * 解析 Motorola MSB 格式的核心算法。
 * 算法描述：从最高位(MSB)开始，在二维比特矩阵中按“之”字形逆向抽取比特。
 *
 * @param msbStartBit MSB 格式下的起始位索引
 * @param bitLength 信号位长度
 * @param matrix 一维化的原始比特矩阵
 * @return [Long] 返回总线未处理值
 */
private fun parseMotorolaMsb(msbStartBit: Int, bitLength: Int, matrix: ByteArray): Long {
    // 1. 使用 ByteArray 初始化全0的数组，存储解析出的信号比特
    val signalBits = ByteArray(bitLength) { 0 }

    // 记录信号跨越的行数
    var rowCount = 0
    // 记录信号在矩阵中的下标。如果是MSB，那么 motorIndex 会从 MSB 的位置一直移动到 LSB 。
    var motorIndex: Int
    /* 核心计算公式：索引motorIndex = (MSB起始位msbStartBit - 当前循环i) + (跨越行数rowCount * 16)
     * 这模拟了在二维矩阵（8 字节 x 8比特，共64位，编号0-63）中从右向左、从下到上的移动  */
    for (i in 0 until bitLength) {
        motorIndex = (msbStartBit - i) + (rowCount * 16)

        // 边界检查：使用 require 函数在条件不满足时抛出 IllegalArgumentException，这是 Kotlin 的惯用法
        require(motorIndex in matrix.indices) {
            "计算摩托罗拉值时下标越界。msbStartBit=$msbStartBit, bitLength=$bitLength, 计算出的index=$motorIndex, 矩阵大小=${matrix.size}" }

        // 当索引移动到当前行的末尾（索引是8的倍数）时，进入下一“行”
        if (motorIndex % 8 == 0) { rowCount++ }

        // 将从矩阵中取出的比特（matrix[motorIndex]）放入信号数组的正确位置。
        // 因为 i 从 0 开始对应信号的 MSB，所以应放入 (bitLength - 1 - i) 的位置（即数组末尾向前）。
        signalBits[bitLength - 1 - i] = matrix[motorIndex]
    }
    // 将字节按“英特尔格式”（小端，低位字节在前）转换为 Long, 这里不需要处理摩托罗拉格式。
    return signalBits.bitsToLong()
}

/**
 * 根据 LSB 起始位和位长，计算其对应的 MSB 起始位。
 * 这是原 `getMsbByLsb` 函数的 Kotlin 实现。
 * 算法是 MSB 解析的“逆过程”：从 LSB 开始正向模拟，最后的 motorIndex 即为等效的 MSB 起始位。
 */
fun Int.lsbStartBitToMsb(bitLength: Int): Int {
    val lsbStartBit = this
    // 记录信号跨越的行数
    var rowCount = 0
    // 记录信号在矩阵中的下标 。如果是 LSB ，那么 motorIndex 会从 LSB 的位置一直移动到 MSB 。
    var motorIndex = 0

    for (i in 0 until bitLength) {
        // 核心计算公式：索引 = (LSB起始位 + 当前循环i) - (跨越行数 * 16)
        motorIndex = (lsbStartBit + i) - (rowCount * 16)
        // 当索引移动到当前行的开头（索引 % 8 == 7）时，进入下一“行”
        if (motorIndex % 8 == 7) { rowCount++ }
    }
    return motorIndex
}
