package dataModel.models

import kotlin.collections.iterator

/**  单个dbc对象 */
class CanDbc {
    /** 标签 */
    var dbcTag: String = ""
    /** 节点列表 */
    var nodeSet: MutableSet<String> = hashSetOf()
    /** 消息列表，键为消息ID的16进制表示，值为消息对象 */
    var msgMap: MutableMap<String, CanMessage> = mutableMapOf()
    /** 波特率 */
    var baudRate: Int = 500

    fun set(canMsg: CanMessage) = msgMap.put(canMsg.msgId.toString(16), canMsg)


    /** 根据索引(添加顺序)获取消息，用于在添加信号时获取刚插入的消息。超出范围返回 null */
    fun getMsgAt(index: Int): CanMessage? = msgMap.entries.elementAtOrNull(index)?.value
    /** 根据信号名称获取一个信号（遍历所有消息） */
    fun getSignal(signalTag: String): CanSignal? = msgMap.values.firstNotNullOfOrNull { message -> message.signalMap[signalTag] }
    /** 根据信号名称和报文id获取一个信号 */
    fun getSignal(messageTag: String, signalTag: String): CanSignal? = msgMap[messageTag]?.signalMap?.get(signalTag)

    override fun toString(): String = buildString {
        append("CanDbc{ dbcTag : ").append(dbcTag).append(";\n")
        for (msg in msgMap.values) {
            append("CanMessage{名称:").append(msg.msgName)
                .append(", 信号列表:").append(msg.signalMap.values).append("}\n")
        }
        append("}; ")
    }

    /** 获取CAN通道所有报文信息 */
    fun getChannelInfo(): String = buildString {
        for ((_, msg) in msgMap) {
            append(msg.getMsgBaseInfo())
            for ((_, sig) in msg.signalMap) append(sig.getSignalInfo())
        }
    }
}
