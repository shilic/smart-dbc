package dataModel.models

import dataModel.dataEnums.*
import dataModel.dataEnums.CANByteOrder.Intel
import dataModel.dataEnums.CANByteOrder.MotorolaLSB
import dataModel.dataEnums.CANByteOrder.MotorolaMSB
import dataModel.dataEnums.GenSigSendType.Cyclic
import dataModel.dataEnums.GenSigSendType.IfActive
import dataModel.dataEnums.GenSigSendType.IfActiveWithRepetition
import dataModel.dataEnums.GenSigSendType.NoSigSendType
import dataModel.dataEnums.GenSigSendType.OnChange
import dataModel.dataEnums.GenSigSendType.OnChangeWithRepetition
import dataModel.dataEnums.GenSigSendType.OnWrite
import dataModel.dataEnums.GenSigSendType.OnWriteWithRepetition
import dataModel.dataEnums.MatrixGroupType.CustomGroup
import dataModel.dataEnums.MatrixGroupType.DefaultGroup
import dataModel.dataEnums.MatrixGroupType.GroupFlag
import dataModel.services.IDbcElement
import io.github.shilic.smartGrid.core.*

/** 用于描述单个信号 */
@GridSheetBind(gridSheetType = GridSheetType.SubSignal)
class CanSignal: IValueTable, IDbcElement, IGridRowData {
    // ++++++++++++++++ IGridRowData 接口实现 +++++++++++++++++
    override val gridKey: String get() = signalName
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    // ----------------------- 基本信息 ----------------------
    /** 信号名称 */
    @GridColumnBind(headerText = "SignalName", pattern = "SignalName|信号名称", valueType = GridValueType.Text, keyword = true)
    var signalName: String =  ""
    /** 信号注释 */
    @GridColumnBind(headerText = "SignalDescription", pattern = "SignalDescription|信号描述|信号注释", valueType = GridValueType.Text)
    var signalComment: String = ""

    // ----------------------- 信号排列 -------------------------
    /** 报文矩阵中的分组类型
     *
     * [GroupFlag] 分组标志位， 返回 M ;
     *
     * [DefaultGroup] 默认分组 , 返回空字符串 ;
     *
     * [CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ;
     *
     *  */
    @GridColumnBind(headerText = "GroupType ", pattern = "GroupType|分组类型", valueType = GridValueType.Custom, customAdapterName = "GroupType")
    var groupType: MatrixGroupType = MatrixGroupType.DefaultGroup
    /** 排列格式。
     *
     * [Intel] : 低位存低位, 显示 1 ;
     *
     * [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致
     *
     * [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致
     * */
    @GridColumnBind(headerText = "ByteOrder", pattern = "ByteOrder|排列格式", valueType = GridValueType.Enum)
    var byteOrder: CANByteOrder = CANByteOrder.CANByteOrderNotDefine
    /** 信号发送类型
     *
     *  [Cyclic] 周期型, 值 = 0
     *
     *  [OnWrite] 写入型， 值 = 1
     *
     *  [OnWriteWithRepetition] 写入型(重复型) ， 值 = 2
     *
     *  [OnChange] 事件型， 值 = 3
     *
     *  [OnChangeWithRepetition] 变化型(重复型) ，值 = 4
     *
     *  [IfActive] 激活型 ，值 = 5
     *
     *  [IfActiveWithRepetition] 激活型(重复型) ，值 = 6
     *
     *  [NoSigSendType] 未定义,  值 = 7
     * */
    @GridColumnBind(headerText = "SignalSendType", pattern = "信号发送类型|((Signal|signal|SIGNAL)\\s*(Send|send|SEND)\\s*(Type|type|TYPE))", valueType = GridValueType.Enum)
    var genSigSendType: GenSigSendType = GenSigSendType.NoSigSendType
    /** 起始字节 byte */
    val startByte: Int get() = startBit / 8
    /** 起始位 bit; 注意，当数据排列格式为 motorola 时，存入其中的起始位只能是 MSB 的位置  */
    @GridColumnBind(headerText = "StartBit", pattern = "起始位|((Start|start|START)\\s*(Bit|bit|BIT))", valueType = GridValueType.Number)
    var startBit: Int = 0
    /** 信号长度  BitLength(Bit) 会用于最大值最小值的计算 */
    @GridColumnBind(headerText = "BitLength", pattern = "信号长度|((Bit|bit|BIT)\\s*(Length|length|LENGTH))", valueType = GridValueType.Number)
    var bitLength: Int = 0
    /** 数据类型，
     *
     * Unsigned 无符号 , 显示+
     *
     * Signed 有符号， 显示 -
     *
     * Float 浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 1
     *
     * Double 双精度浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 2
     * */
    @GridColumnBind(headerText = "DataType", pattern = "数据类型|((Data|data|DATA)\\s*(Type|type|TYPE))", valueType = GridValueType.Enum)
    var dataType: CANDataType = CANDataType.CANDataTypeNotDefine
    /** factor 精度(精度不可以为0，否则无意义) ; 物理值 = 原始值 * factor + offset */
    @GridColumnBind(headerText = "Factor", pattern = "精度|分辨率|Resolution|resolution|Factor|factor|FACTOR", valueType = GridValueType.Number)
    var factor: Double = 1.0
    /** offset 偏移量 (通常为负数) ； 物理值 = 原始值 * factor + offset */
    @GridColumnBind(headerText = "Offset", pattern = "偏移量|offset|Offset|OFFSET", valueType = GridValueType.Number)
    var offset: Double = 0.0

    // ------------------------ 物理值 -----------------------
    /** 物理最小值 */
    @GridColumnBind(headerText = "SignalMinValuePhys", pattern = "物理最小值|SignalMinValuePhys|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Min|MIN|min)\\s*(Value|value|VALUE)\\s*(Phys|phys|PHYS|Phy|PHY|phy))", valueType = GridValueType.Number)
    var signalMinValuePhys: Double = 0.0
    /** 物理最大值 */
    @GridColumnBind(headerText = "SignalMaxValuePhys", pattern = "物理最大值|SignalMaxValuePhys|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Max|MAX|max)\\s*(Value|value|VALUE)\\s*(Phys|phys|PHYS|Phy|PHY|phy))", valueType = GridValueType.Number)
    var signalMaxValuePhys: Double = 0.0
    /** 物理初始值 */
    @GridColumnBind(headerText = "InitialValuePhys", pattern = "物理初始值|InitialValuePhys|((Initial|Initial|INITIAL|INI|Ini|ini|Init|INIT|init)\\s*(Value|value|VALUE)\\s*(Phys|phys|PHYS|Phy|PHY|phy))", valueType = GridValueType.Number)
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

    // +++++++++++++++ IValueTable (值描述接口) 实现 +++++++++++++++++++
    override var valueTable: MutableMap<Int, String> = mutableMapOf()
    override var aValue: String = ""

    /** 接收节点列表 */
    var sigReceiveNodeSet: MutableSet<String> = mutableSetOf()
    /** 信号接收节点信息 */
    val sigReceiveNodeDbcCode: String get() = sigReceiveNodeSet.takeIf { it.isNotEmpty() }?.joinToString(",") ?: DEFAULT_NODE

    var currentValue: Double = 0.0

    var valid: Boolean = true

    // ++++++++++++++++ 实现 IDbcElement , 用于序列化到文件 ++++++++++++++++++
    override val dbcKey: String get() = signalName
    /** 返回形如
     *
     * SG_ test_Signal_14 m2 : 24|8@1+ (0.1,-5.55) [-5|20.5] ""  Cabin,CCS
     *
     * 的DBC编码 */
    override val dbcValue: String get() =
        " SG_ $signalName : $startBit|$bitLength@${byteOrder.dbcValue}${dataType.dbcValue} " +
            "($factor,$offset) [$signalMinValuePhys|$signalMaxValuePhys] \"${unit}\" $sigReceiveNodeDbcCode"

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
