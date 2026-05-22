package dataModel.models

import core.IGridRowData
import dataModel.dataEnums.CanMsgIdType
import dataModel.dataEnums.GenMsgSendType
import numberUtils.toHexStr

/** 用于描述消息 Message */
class CanMessage: IGridRowData  {

    // ---------------- IGridRowData 接口实现 -----------------
    override val gridKey: String get() = msgId.toHexStr()
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    /** 报文名称 */
    var msgName: String = ""
    /** 报文标识符 */
    var msgId: Int = 0
    /** 报文标识符的DBC编码。标准帧 = msgId；扩展帧 = msgId + 0x8000_0000L */
    var msgIdCode: Long = 0
    /** 帧类型 */
    var msgIdType: CanMsgIdType = CanMsgIdType.Extended
    /** 报文长度(byte) */
    var msgLength: Int = 0
    /** 发送节点名称 */
    var nodeName: String = ""
//    @Deprecated("暂时不打算增加对诊断报文的识别")
//    var msgType: MsgType = MsgType.Normal

    var genMsgSendType: GenMsgSendType = GenMsgSendType.Cycle
    var msgCycleTime: Int = 0
    var msgComment: String = ""
    var msgSendNodeList: MutableSet<String> = hashSetOf()

    /** 信号列表，键为信号名称 */
    val signalMap: MutableMap<String, CanSignal> = mutableMapOf()

    override fun toString() = "CanMessage{名称:$msgName}"

    fun getMsgBaseInfo(): String = buildString {
        append("\n--消息名称:$msgName, 报文标识符:${msgId.toString(16)}")
        append(", 信号ID类型:$msgIdType, 发送类型:$genMsgSendType")
        append(", 周期(ms):$msgCycleTime, 长度(byte):$msgLength")
        append(", 注释:$msgComment, 发送节点:$nodeName")
        append(", 信号数量:${signalMap.size}\n")
    }

    /** 获取报文所有信号的值，用于校验数据是否修改成功 */
    fun getMsgValue(): String = buildString {
        append("报文名称：").append(msgName).append(";\n")
        signalMap.values.forEach { sig ->
            //append("信号 : ${sig.signalName} = ${sig.readValue()};\n")
        }
        TODO()
    }

    companion object {
        /** 扩展帧 idCode 转 id */
        @JvmStatic fun transIdCodeToID(msgIDCode: Long): Int = (msgIDCode - 0x8000_0000L).toInt()
        /** id 转 扩展帧 idCode */
        @JvmStatic fun transIdToIdCode(msg_ID: Long): Long = msg_ID + 0x8000_0000L
    }
}
