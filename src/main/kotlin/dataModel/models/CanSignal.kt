package dataModel.models

import core.IGridRowData
import core.IValueTable
import dataModel.dataEnums.*
import dataModel.services.IDbcElement

/** 用于描述单个信号 */
class CanSignal: IValueTable, IDbcElement, IGridRowData {
    // ---------------- IGridRowData 接口实现 -----------------
    override val gridKey: String get() = signalName
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    // ----------------------- 基本信息 ----------------------
    /** 信号名称 */
    var signalName: String =  ""
    /** 信号注释 */
    var signalComment: String = ""

    // ----------------------- 信号排列 -------------------------
    /** 分组类型 */
    var groupType: MatrixGroupType = MatrixGroupType.DefaultGroup
    /** 排列格式 Intel 或 Motorola */
    var byteOrder: CANByteOrder = CANByteOrder.CANByteOrderNotDefine
    var genSigSendType: GenSigSendType = GenSigSendType.GenSigSendTypeNotDefine
    val startByte: Int get() = startBit / 8
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

    // ------------------------ 物理值 -----------------------
    /** 物理最小值 */
    var signalMinValuePhys: Double = 0.0
    /** 物理最大值 */
    var signalMaxValuePhys: Double = 0.0
    /** 物理初始值 */
    var signalInitialValuePhys: Double = 0.0

    // -------------- 原始值(总线值/未处理值) ----------------
    /** 总线最小值 */
    var signalMinValueHex: Double = 0.0
    /** 总线最大值 */
    var signalMaxValueHex: Double = 0.0
    /** 总线初始值 */
    var signalInitialValueHex: Double = 0.0
    /** 总线无效值(只有总线无效值，没有物理无效值，因为总线无效就没必要计算物理值) */
    var invalidValueHex: Int = 0

    /** 单位 */
    var unit: String = ""

    // ------------------ 值描述 --------------------
    override var valueTable: MutableMap<Int, String> = mutableMapOf()
    override var aValue: String = ""

    /** 接收节点列表 */
    var sigReceiveNodeSet: MutableSet<String> = mutableSetOf()
    /** 信号接收节点信息 */
    val sigReceiveNodeDbcCode: String get() = sigReceiveNodeSet.takeIf { it.isNotEmpty() }?.joinToString(",") ?: DEFAULT_NODE

    var currentValue: Double = 0.0

    var valid: Boolean = true

    // ------------------- 实现 IDbcElement , 用于序列化到文件 --------------------
    override val dbcKey: String get() = signalName
    override val dbcValue: String get() = " SG_ $signalName : $startBit|$bitLength@${byteOrder.dbcValue}${dataType.dbcValue} ($factor,$offset) [$signalMinValuePhys|$signalMaxValuePhys] \"${unit}\" $sigReceiveNodeDbcCode"
    override fun toString() = dbcValue
    fun signalInfo(): String = buildString {
        append("{ 信号名称:$signalName, 多路复用:${groupType.dbcKey}, 组号:${groupType.dbcValue}")
        append(", 注释:$signalComment, 排列方式:$byteOrder, 起始位:$startBit")
        append(", 长度:$bitLength, 数据类型:$dataType, 精度:$factor, 偏移量:$offset")
        append(", 物理最小值:$signalMinValuePhys, 物理最大值:$signalMaxValuePhys")
        append(", 单位:$unit, 接收节点:${sigReceiveNodeDbcCode} }")
    }
    companion object { const val DEFAULT_NODE = "Vector__XXX" }
}
