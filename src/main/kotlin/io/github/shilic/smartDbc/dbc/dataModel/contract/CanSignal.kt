package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartDbc.can.accessors.*
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.utils.*
import io.github.shilic.smartGrid.core.*

/** 提供不可变的 CanSignal */
interface CanSignal: IValueTable, IDbcElement, IGridRowData, CanAccessor {
    // +++++++++++++ IGridRowData 接口实现， 只重写 gridKey 定义，其他保持默认 ++++++++++++++
    override val gridKey: String get() = signalName
    override val dbcKey: String get() = signalName

    // ----------------------- 基本信息 ----------------------
    /** 信号名称 */
    val signalName: String
    /** 信号注释 */
    val signalComment: String

    // ----------------------- 信号排列 -------------------------
    /** 报文矩阵中的分组类型
     *
     * [MatrixGroupType.GroupFlag] 分组标志位， 返回 M ;
     *
     * [MatrixGroupType.DefaultGroup] 默认分组 , 返回空字符串 ;
     *
     * [MatrixGroupType.CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ;
     *
     *  */
    val groupType: MatrixGroupType
    /** 排列格式。
     *
     * [CANByteOrder.Intel] : 低位存低位, 显示 1 ;
     *
     * [CANByteOrder.MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致
     *
     * [CANByteOrder.MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致
     * */
    val byteOrder: CANByteOrder
    /** 信号发送类型
     *
     *  [GenSigSendType.Cyclic] 周期型, 值 = 0
     *
     *  [GenSigSendType.OnWrite] 写入型， 值 = 1
     *
     *  [GenSigSendType.OnWriteWithRepetition] 写入型(重复型) ， 值 = 2
     *
     *  [GenSigSendType.OnChange] 事件型， 值 = 3
     *
     *  [GenSigSendType.OnChangeWithRepetition] 变化型(重复型) ，值 = 4
     *
     *  [GenSigSendType.IfActive] 激活型 ，值 = 5
     *
     *  [GenSigSendType.IfActiveWithRepetition] 激活型(重复型) ，值 = 6
     *
     *  [GenSigSendType.NoSigSendType] 未定义,  值 = 7
     * */
    val genSigSendType: GenSigSendType
    /** 起始字节 byte  (不需要额外解析，通过起始位进行计算) */
    @Suppress("UNUSED")
    val startByte: Int get() = startBit / 8
    /** 起始位 bit; 注意，当数据排列格式为 motorola 时，存入其中的起始位只能是 MSB 的位置  */
    val startBit: Int
    /** 信号长度  BitLength(Bit); 会用于最大值最小值的计算 */
    val bitLength: Int
    /** 数据类型，
     *
     * [CANDataType.Unsigned] 无符号 , 显示+
     *
     * [CANDataType.Signed] 有符号， 显示 -
     *
     * [CANDataType.Float] 浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 1
     *
     * [CANDataType.Double] 双精度浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 2
     * */
    val dataType: CANDataType
    /** factor 精度 (精度factor作为除数不可以为0, 否则无意义) ; 物理值 = 原始值 * factor + offset */
    val factor: Double
    /** offset 偏移量 (通常为负数) ； 物理值 = 原始值 * factor + offset */
    val offset: Double

    // ------------------------ 物理值 -----------------------
    /** 物理最小值 */
    val signalMinValuePhys: Double
    /** 物理最大值 */
    val signalMaxValuePhys: Double
    /** 物理初始值 */
    val initialValuePhys: Double

    // -------------- 原始值(总线值/未处理值) ----------------
    /** 总线最小值 (根据信号长度和物理值计算) */
    val signalMinValueHex: Long
    /** 总线最大值  (根据信号长度和物理值计算) */
    val signalMaxValueHex: Long
    /** 总线初始值  (根据信号长度和物理值计算) */
    val initialValueHex: Long
    /** 总线无效值 (只有总线无效值，没有物理无效值，因为总线无效就没必要计算物理值) */
    val invalidValueHex: Long

    /** 单位 */
    val unit: String

    // ------------------------------- 节点 ----------------------------------
    /** 信号接收节点列表 (使用自定义逻辑从表格解析) */
    val sigReceiveNodeSet: Set<String>
    /** 信号接收节点集合DBC值 (使用逗号分隔, 自动组合成字符串) */
    val sigReceiveNodesDbcValue: String get() = sigReceiveNodeSet.takeIf { it.isNotEmpty() }?.joinToString(",") ?: DEFAULT_NODE

    // ++++++++++++++++ 实现 IDbcElement , 用于序列化到文件 ++++++++++++++++
    /** 返回DBC编码, 形如
     *
     * SG_ test_Signal_14 m2 : 24|8@1+ (0.1,-5.55) [-5|20.5] ""  Cabin,CCS
     * */
    override val dbcValue: String get() =
        " SG_ $signalName ${groupType.dbcValue} : $startBit|$bitLength@${byteOrder.dbcValue}${dataType.dbcValue} " +
                "($factor,$offset) [$signalMinValuePhys|$signalMaxValuePhys] \"${unit}\" $sigReceiveNodesDbcValue"

    // ======================== 调试方法 =========================
    /** 获取基本信息 */
    val baseInfo: String get() = "CanSignalBaseInfo(signalName='$signalName', signalComment='$signalComment', " +
            "startBit=$startBit, bitLength=$bitLength, byteOrder=$byteOrder, dataType=$dataType, groupType='${groupType.dbcValue}', " +
            "factor=$factor, offset=$offset, " +
            "signalMinValuePhys=$signalMinValuePhys, signalMaxValuePhys=$signalMaxValuePhys, invalidValueHex=$invalidValueHex, " +
            "unit='$unit', valueTable=$valueTable)"
    /** 获取值信息 */
    val valueInfo: String get() = "($signalName = $currentTextValue)"

    /** 将物理值转换为16进制总线值;
     *
     * 公式: 物理值 = 原始值 * factor + offset  */
    fun phyToHex(phyValue: Double) : Long = phyValue.phyToHex(factor, offset)
    /** 将16进制总线值转换为物理值
     *
     * 公式: 物理值 = 原始值 * factor + offset   */
    fun hexToPhy(hexValue: Long) : Double = hexValue.hexToPhy(factor, offset)
    /* 这里必须要将物理值转换为总线值：因为有时候存在值描述和精度偏移量混用的情况, 例如 '0~100' 表示车速，'0xFF' 表示无效；
     * 故此时, 若需要综合计算值是否在值描述中，必须转换为总线值。
     * 也就是说，值描述中的索引，必须是总线值(忽略精度偏移量)，而不是物理值。 */
    /** 将物理值转换为文本值;
     *
     * 综合精度偏移量和值描述进行转换
     * */
    fun phyToText(phyValue: Double) : String = phyValue.phyToText(factor, offset, valueTable)
    /** 将文本值转换为物理值 */
    fun textToPhy(text: String) : Double = text.textToPhy(factor, offset, valueTable)
    /** 将16进制总线值转换为文本值 */
    fun hexToText(hexValue: Long): String = hexValue.toInt().hexToText(valueTable)
    /** 将文本值转换为16进制总线值 */
    fun textToHex(text: String): Long = text.textToHex(valueTable).toLong()

    override val validity: Boolean get() = currentHexValue != invalidValueHex
}