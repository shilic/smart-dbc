package dataModel.models

import dataModel.dataEnums.CANByteOrder
import dataModel.dataEnums.CANDataType
import dataModel.dataEnums.MatrixGroupType

/**
 * 用于描述单个信号
 */
class CanSignal {
    /** 信号名称 */
    var signalName: String =  ""
    /** 信号注释 */
    var signalComment: String = ""


    /** 分组类型 */
    var groupType: MatrixGroupType = MatrixGroupType.DefaultGroup
    /** 排列格式 Intel 或 Motorola */
    var byteOrder: CANByteOrder = CANByteOrder.Intel
    /** 起始位 bit */
    var startBit: Int = 0
    /** 信号长度 BitLength(Bit) */
    var bitLength: Int = 0
    /** 数据类型：无符号/有符号 */
    var dataType: CANDataType = CANDataType.Unsigned
    /** 精度。物理值 = 原始值 * factor + offset */
    var factor: Double = 1.0
    /** 偏移量。物理值 = 原始值 * factor + offset */
    var offset: Double = 0.0
    /** 物理最小值 */
    var signalMinValuePhys: Double = 0.0
    /** 物理最大值 */
    var signalMaxValuePhys: Double = 0.0
    /** 单位 */
    var unit: String = ""
    /** 接收节点列表 */
    var sigReceiveNodeSet: MutableSet<String> = mutableSetOf()





    var strGroupValue: String = ""
    var iniValuePhys: Double = 0.0
    var iniValueHex: Double = 0.0
    @Deprecated("单独使用值而不绑定模型时使用")
    @Volatile
    var currentValue: Double = 0.0
    @Volatile
    var valid: Boolean = true
    @Deprecated("后续增加无效值解析")
    var validValue: Int = 0

    override fun toString() = signalName

    fun getSignalInfo(): String = buildString {
        append("{ 信号名称:$signalName, 多路复用:$groupType, 组号:${groupType.value}")
        append(", 注释:$signalComment, 排列方式:$byteOrder, 起始位:$startBit")
        append(", 长度:$bitLength, 数据类型:$dataType, 精度:$factor, 偏移量:$offset")
        append(", 物理最小值:$signalMinValuePhys, 物理最大值:$signalMaxValuePhys")
        append(", 单位:$unit, 接收节点:${getReceiveNodeListCode()} }")
    }

    private fun getReceiveNodeListCode(): String =
        sigReceiveNodeSet.takeIf { it.isNotEmpty() }?.joinToString(",") ?: DEFAULT_NODE

    companion object { const val DEFAULT_NODE = "Vector__XXX" }
}
