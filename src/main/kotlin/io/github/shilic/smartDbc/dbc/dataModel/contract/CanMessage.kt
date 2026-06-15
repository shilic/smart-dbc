package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.numberUtils.toHexStr
import io.github.shilic.smartDbc.dbc.attributes.contract.DbcAttributeValue
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartGrid.core.*

/**
 * 提供只读的的 [CanMessage] ;
 * */
interface CanMessage : IGridRowData, IDbcElement, SubDataOwner  {
    // +++++++++++++ IGridRowData 接口实现， 只重写 gridKey 定义，其他保持默认 ++++++++++++++
    override val gridKey: String get() = msgIdToKey(msgId)
    override val dbcKey: String get() = msgIdToKey(msgId)

    // ----------------------- 基本信息 -----------------------
    /** 报文名称, 非空 */
    val msgName: String
    // TODO(这个地方，由于excel表和dbc文件的定义存在较大差异，现有excel表通过枚举定义诊断报文和网络管理报文，忽略了扩展帧和标准帧识别；
    //  但是dbc文件中，通过单独的布尔值来定义诊断报文和网络管理报文；这里暂时不打算适配excel表，以dbc文件为准。)
    /** 扩展帧标志
     *
     *  [CanExternFlag.Standard] 标准帧， [CanExternFlag.intValue] = 0 ；范围： 0x0~0x7FF ; 在DBC文件中 = msgId ;
     *
     * [CanExternFlag.Extended] 扩展帧， [CanExternFlag.intValue] = 1 ；范围：0x0~0x1FFF_FFFF ; 在DBC文件中 = msgId + 0x8000_0000L ;
     *
     *  @property CanExternFlag.intValue 整型值
     * */
    val msgIdType: CanExternFlag
    /** 报文标识符; 报文id  */
    val msgId: Int
    /** 报文标识符的DBC编码。标准帧 = [msgId]；扩展帧 = [msgId] + 0x8000_0000L */
    val longIdCode: Long get() = transMsgIdToLongIdCode(msgId, msgIdType)
    /** 报文发送模式
     *
     *  [GenMsgSendType.Cycle] : 周期型, 序号 0 ;
     *
     *  [GenMsgSendType.Event] : 事件型, 序号 1 ;
     *
     *  [GenMsgSendType.IfActive] : 激活型, 序号 2 ;
     *
     *  [GenMsgSendType.CE] : 持续型, 序号 3 ;
     *
     *  [GenMsgSendType.CA] : 持续型(激活型) , 序号 4
     *
     *  */
    val genMsgSendType: GenMsgSendType
    /** 报文周期时间 (单位：毫秒) */
    val msgCycleTime: Int
    /** 报文长度(单位：byte) */
    val msgLength: Int
    /** 报文备注 */
    val msgComment: String

    // -------------------------- 节点信息 ------------------------
    // 在通信矩阵中，报文发送节点只能有一个，但是可以有有多个接收节点。
    /** 发送节点名称 */
    val nodeName: String
    /** 报文接收节点 */
    val msgReceiveNodeSet: Set<String>

    /** 信号列表 ; 键指信号的名称, 值指的是信号 */
    val signalMap: Map<String, CanSignal>

    // ----------------------- 自定义属性 ---------------------
    /** 自定义属性值的集合 */
    val attributeValueMap: Map<String, DbcAttributeValue>

    // ++++++++++++++++ 实现 IDbcElement , 用于序列化到文件 ++++++++++++++
    /** 返回DBC编码, 形如
     *
     * BO_ 2560107544 CCSToAC1: 8 CCS
     * */
    override val dbcValue: String get() = "$BO_ $longIdCode ${msgName}: $msgLength $nodeName"
    /** 输出报文节点的DBC编码：
     *
     * 例如：BO_TX_BU_ 2560107544 : Cabin,Test;
     * */
    val nodesLine: String get() = "$BO_TX_BU_ $longIdCode : ${msgReceiveNodeSet.joinToString(",")};"
    /** 输出注释编码：
     *
     * 例如：CM_ BO_ 2560104484 "上装发给中控屏1(发给网关，网关转给中控屏)。";
     * */
    val commentLine: String get() = "$CM_ $BO_ $longIdCode \"$msgComment\";"
    // ========================= 调试方法 ==========================
    val baseInfo: String get() = "${CanMessage::class.simpleName}(${::msgName.name}='$msgName', " +
            "${::msgId.name}=${msgIdToKey(msgId)}, ${::msgIdType.name}=$msgIdType, " +
            "${::genMsgSendType.name}=$genMsgSendType, ${::msgCycleTime.name}=$msgCycleTime, " +
            "${::msgLength.name}=$msgLength, ${::msgComment.name}=$msgComment, " +
            "${::nodeName.name}='$nodeName', ${::msgReceiveNodeSet.name}=$msgReceiveNodeSet, signalMap.size=${signalMap.size})"
    val valueInfo: String get() = "(${CanMessage::class.simpleName}($msgName).Values=${signalMap.values.map { it.valueInfo }})"
    operator fun get(signalName: String): CanSignal? = signalMap[signalName]
    companion object {
        /** 根据 id 转 key 字符串 */
        fun msgIdToKey(msgId: Int): String = msgId.toHexStr()
        /** 扩展帧 longIdCode 转 id */
        fun transLongIdCodeToMsgId(longIdCode: Long): Int = when {
            // 标准帧直接转换
            longIdCode < 0x8000_0000L -> longIdCode.toInt()
            else -> (longIdCode - 0x8000_0000L).toInt()
        }
        /** 扩展帧 longIdCode 转 id */
        fun transLongIdCodeToMsgId(longIdCode: Long, msgIdType: CanExternFlag): Int = when (msgIdType) {
            // 标准帧直接转换
            CanExternFlag.Standard -> longIdCode.toInt()
            CanExternFlag.Extended -> (longIdCode - 0x8000_0000L).toInt()
        }
        /** id 转 扩展帧 longIdCode */
        fun transMsgIdToLongIdCode(msgId: Int, msgIdType: CanExternFlag): Long = when(msgIdType) {
            CanExternFlag.Standard -> msgId.toLong()
            CanExternFlag.Extended -> msgId + 0x8000_0000L
        }
        /** 根据 id 计算帧ID类型;
         *
         * 帧ID类型：标准帧 0x0~0x7FF ; 扩展帧 0x0~0x1FFF_FFFF  */
        fun calculateCanMsgIdTypeById(msgId: Int): CanExternFlag = when(msgId){
            in 0x0..0x7FF -> CanExternFlag.Standard
            else -> CanExternFlag.Extended
        }
    }
}