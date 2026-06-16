package io.github.shilic.smartDbc.valueConverter

import io.github.shilic.numberUtils.*
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.can.models.canFrame.models.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*

/* CAN报文输入接口
* 这里需要解决一下几个问题：
* 1.1 对msg：解码报文: 输入报文, 将数据解析到DBC和绑定对象中；
* 1.2 对msg：编码报文: 输出报文, 将DBC和绑定对象中的数据编码成报文；
*
* 2.1 对于dbc：输入报文，将报文解析到DBC中；
* 2.2 对于dbc：输出报文，将DBC中的数据编码成报文；
*
* 这里明确一下，什么时候才需要处理摩托罗拉格式：
* 1. 将bytes转换为bits，或者将bits转换bytes, 不需要处理摩托罗拉格式。
* 2. 将无符号Hex转换为有符号phy，也不需要处理摩托罗拉格式。
* 3. 从bits数组从取出值组成总线值时, 需要处理摩托罗拉格式!! 因为英特尔排序是有序的, 而此时的摩托罗拉排序是乱序的, 不是倒序，而是乱序。
*  */

// -------------------- 对于 dbc  ----------------------
/** 解码报文: 输入 CanFrame 报文, 将数据解析到DBC和绑定对象中； */
fun DataBaseCan.decodeCanFrame(canFrame: CanFrame) = this[canFrame.msgId]?.decodeCanFrame(canFrame)
/** 解码报文: 输入 Bytes 报文, 将数据解析到DBC和绑定对象中； */
fun DataBaseCan.decodeBytes(msgId: Int, data: ByteArray) = this[msgId]?.decodeBytes(data)
/** 编码报文: 输出 CanFrame 报文, 将DBC中的数据编码成报文； */
fun DataBaseCan.encodeCanFrame(msgId: Int, newOwner: Any? = null): CanFrameData = this[msgId]?.encodeCanFrame(newOwner) ?: CanFrameData.empty(msgId)
/** 编码报文: 输出 Bytes 报文, 将DBC中的数据编码成报文； */
fun DataBaseCan.encodeBytes(msgId: Int, newOwner: Any? = null): ByteArray = this[msgId]?.encodeBytes(newOwner) ?: ByteArray(8)


// -------------------- 对于 msg, 不关心报文ID，只关心数据输入输出 --------------------------
/** 解码报文: 输入 CanFrame 报文; 将数据解析到 CanMessage 和绑定对象中；
 *
 * 函数会查找到报文ID，然后进行解码。如果报文ID不存在，则不执行。
 * */
fun CanMessage.decodeCanFrame(canFrame: CanFrame) = takeIf { this.msgId == canFrame.msgId }?.decodeBytes(canFrame.data)
/** 解码报文: 输入 bytes 报文; 将数据解析到 CanMessage 和绑定对象中；
 *
 * 这里没有判断报文ID, 会直接将所有收到的报文进行一个解析*/
fun CanMessage.decodeBytes(canData: ByteArray) {
    // 1. 将字节数组转换为bits数组, 不需要处理摩托罗拉格式。
    val bits: ByteArray = canData.toBits()
    for (signal in signalMap.values) {
        // 2. 从bits数组从取出值, 转换成总线值, 需要处理摩托罗拉格式!!
        val hexValue = bits.bitsToHexValue(signal.byteOrder, signal.startBit, signal.bitLength)
        // 3. 将无符号Hex转换为有符号phy，也不需要处理摩托罗拉格式
        val phyValue = hexValue.hexToPhy(signal.factor, signal.offset)
        // 4. 将总线值写入 signal 中
        signal.writeCanValue(phyValue)
    }
}
/** 编码报文: 输出 CanFrame 报文;
 *
 * 将 CanMessage 和绑定对象中的数据编码成 CanFrame 报文；
 *
 * 读取CAN值：
 *
 * -> 优先从指定接受者字段读取值; [newOwner] 参数为空时，使用默认接受者
 *
 * -> 如果绑定字段值为空, 其次从DBC对象读取信号值
 * */
fun CanMessage.encodeCanFrame(newOwner: Any? = null): CanFrameData = encodeBytes(newOwner).toCanFrameData(msgId)
/** 编码报文: 输出 bytes 报文;
 *
 * 将 CanMessage 和绑定对象中的数据编码成 bytes 报文；
 *
 * 读取CAN值：
 *
 * -> 优先从指定接受者字段读取值; [newOwner] 参数为空时，使用默认接受者
 *
 * -> 如果绑定字段值为空, 其次从DBC对象读取信号值
 * */
fun CanMessage.encodeBytes(newOwner: Any? = null): ByteArray {
    // 根据报文的字节数，创建对应长度的 待填充的 bits 数组
    val msgBits = ByteArray(msgLength * 8)
    for (signal in signalMap.values) {
        // 1. 获取 signal 的总线值；
        val hexValue = signal.readCanValue(newOwner).phyToHex(signal.factor, signal.offset)
        // 注意, 这里不涉及排列格式，所以直接使用英特尔格式转换即可
        val sigBits = hexValue.toBits(bitLength = signal.bitLength)

        // 2. 将 signal 的 bits 值按位填充到 bits 数组中; 需要处理摩托罗拉格式!!
        when(signal.byteOrder) {
            // 英特尔格式直接按照起始位和长度拼接到 msgBits 中。ASCII 和 BCD 格式同样按照英特尔排列处理。
            CanByteOrder.Intel -> System.arraycopy(sigBits, 0, msgBits, signal.startBit, signal.bitLength)
            // 单独处理摩托罗拉格式的填充。这里就是无符号和有符号格式的值需要按照 摩托罗拉格式处理
            else ->  motorolaIntoBits(msgBits, sigBits, signal.startBit, signal.bitLength, signal.byteOrder)
        }
    }
    // 将填充后的 bits 数组转换成字节数组并返回
    return msgBits.bitsToBytes()
}

// ------------------------ 编码时的过程函数 ---------------------------
/**
 * 主入口函数：将信号比特合并到原始比特矩阵中。
 *
 * @param matrix 目标比特矩阵（将被修改）
 * @param sigBits 要插入的信号比特数组
 * @param startBit 信号起始位
 * @param bitLength 信号位长度
 * @param byteOrder 字节序
 * @throws IllegalArgumentException 当参数无效时抛出
 */
fun motorolaIntoBits(matrix: ByteArray, sigBits: ByteArray, startBit: Int, bitLength: Int, byteOrder: CanByteOrder) {
    // 输入验证
    require(bitLength > 0) { "bitLength 必须大于 0" }
    require(sigBits.size >= bitLength) { "信号比特数组长度 (${sigBits.size}) 必须大于位长度 ($bitLength)" }
    require(matrix.isNotEmpty()) { "矩阵不能为空" }
    when (byteOrder) {
        CanByteOrder.MotorolaMSB -> combineMotorolaMsb(matrix, sigBits, startBit, bitLength)
        CanByteOrder.MotorolaLSB -> {
            val msbStartBit = startBit.lsbStartBitToMsb(bitLength)
            combineMotorolaMsb(matrix, sigBits, msbStartBit, bitLength)
        }
        else -> error("必须是摩托罗拉格式，才能被 '${::motorolaIntoBits.name}' 函数解析")
    }
}

/**
 * Motorola MSB 格式的核心合并算法。
 * 算法描述：从信号的最高位(MSB)开始，在二维比特矩阵中按"之"字形逆向放置比特。
 * 这是解析过程的逆操作。
 *
 * @param matrix 目标比特矩阵（将被修改）
 * @param sigBits 信号比特数组，其最高位(MSB)对应 sigBits[bitLength-1]
 * @param msbStartBit MSB 格式下的起始位索引
 * @param bitLength 信号位长度
 * @throws IllegalArgumentException 当索引越界时抛出
 */
private fun combineMotorolaMsb(matrix: ByteArray, sigBits: ByteArray, msbStartBit: Int, bitLength: Int) {
    // 记录信号跨越的行数
    var rowCount = 0

    for (i in 0 until bitLength) {
        // 记录信号在矩阵中的下标。如果是MSB，那么 motorIndex 会从 MSB 的位置一直移动到 LSB 。
        // 核心计算公式：目标索引index = (MSB起始位msbStartBit - 当前循环i) + (跨越行数rowCount * 16)
        // 这模拟了在二维矩阵（8 字节 x 8比特，共64位，编号0-63）中从右向左、从下到上的移动
        val targetIndex = (msbStartBit - i) + (rowCount * 16)
        // 边界检查
        require(targetIndex in matrix.indices) { "计算摩托罗拉合并时下标越界。" +
                "msbStartBit = $msbStartBit, bitLength = $bitLength, 当前 i = $i," +
                "计算出的 targetIndex = $targetIndex, 矩阵大小 = ${matrix.size}" }

        // 当索引移动到当前行的末尾（索引是8的倍数）时，进入下一"行"
        if (targetIndex % 8 == 0) { rowCount++ }

        // 从信号比特数组的末尾开始取位（因为数组末尾是MSB, 序号i是msb, 也就是数据的最高位）;
        // 放置到矩阵的指定位置, 故 这个数据应当存放在 (bitLength - 1 - i ) 这个位置
        matrix[targetIndex] = sigBits[bitLength - 1 - i]
    }
}

// ------------------------ 解码时的过程函数 ---------------------------
/** 综合摩托罗拉格式和英特尔格式，解析bits数组中的数据，并转换为一个总线未处理值。(按位取出时，需要处理摩托罗拉格式)
 *
 * -> 英特尔格式，直接按位取出，并转换为无符号Hex，因为英特尔是连续排列的
 *
 * -> 摩托罗拉格式，需要特殊处理, 处理摩托罗拉的乱序(不是倒序，而是乱序)
 * */
fun ByteArray.bitsToHexValue(byteOrder: CanByteOrder, startBit: Int, bitLength: Int): Long = when (byteOrder) {
    // 英特尔格式，直接按位取出，并转换为无符号Hex，因为英特尔是连续排列的
    CanByteOrder.Intel -> intelBitsToHex(startBit, bitLength)
    // 摩托罗拉格式，需要特殊处理
    else -> motorolaBitsToHex(byteOrder, startBit, bitLength)
}

/** 将数据按bit位取出，并转换为一个 物理值, 不需要处理摩托罗拉格式 */
fun ByteArray.intelBitsToPhy(startBit: Int, bitLength: Int, factor: Double, offset: Double) : Double = copyOfRange(startBit, startBit + bitLength).bitsToLong().hexToPhy(factor, offset)

/** 将数据按bit位取出，并转换为一个 Long 的无符号数 , 不需要处理摩托罗拉格式*/
fun ByteArray.intelBitsToHex(startBit: Int, bitLength: Int): Long = copyOfRange(startBit, startBit + bitLength).bitsToLong()


