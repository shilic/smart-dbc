package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartDbc.dbc.dataModel.models.DbcBaseInfo

import io.github.shilic.smartGrid.core.*

import  io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanMessage as MMsg
import  io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanSignal as MSig
import io.github.shilic.smartDbc.dbc.attributes.contract.MutableDbcAttributeDefinition as MAttr


// 参考了kotlin语言的设计风格，设计了类似的API，区分了可变和不可变的对象。
/**  可修改的 [DataBaseCan] */
interface MutableDataBaseCan<M, S, A> : DataBaseCan, IMutableGridRowData, IMutableGridSpecificSheet, MutableSubDataOwner where M: MMsg<S>, S: MSig, A : MAttr {
    // ------------------------- 基本信息 ---------------------
    override var dbcTag: String
    override var version: String
    override var dbcComment: String
    override var nodeSet: MutableSet<String>
    override var baudRate: Int

    // ----------------------- 自定义属性 ---------------------
    override var attributeMap : MutableMap<String, A>

    // ------------------------ 子数据 ------------------------
    override var msgMap: MutableMap<String, M>
    /** 供外部使用，用于快速设置DBC基础信息 */
    fun setDbcBaseInfo (baseInfo: DbcBaseInfo) {
        this.dbcTag = baseInfo.dbcTag
        this.version = baseInfo.version
        this.dbcComment = baseInfo.dbcComment
        this.nodeSet = baseInfo.nodeSet
        this.baudRate = baseInfo.baudRate
    }
    // =========================  索引器们  ===========================
    /** 添加报文 */
    fun set(canMsg: M) = msgMap.put(canMsg.dbcKey, canMsg)
    /** 根据报文标签获取报文 */
    override operator fun get(messageTag: String): M? = msgMap[messageTag]
    /** 根据消息ID获取消息 */
    override operator fun get(msgId: Int): M? = msgMap[CanMessage.msgIdToKey(msgId)]
    /** 根据索引(添加顺序)获取消息，用于在添加信号时获取刚插入的消息。超出范围返回 null */
    override fun getMsgAt(index: Int): M? = msgMap.entries.elementAtOrNull(index)?.value
    /** 根据信号名称获取一个信号（遍历所有消息） */
    override fun getSignal(signalName: String): S? = msgMap.values.firstNotNullOfOrNull { message -> message.signalMap[signalName] }
    /** 根据信号名称和报文id获取一个信号 */
    override fun getSignal(messageTag: String, signalName: String): S? = msgMap[messageTag]?.signalMap?.get(signalName)
    /** 根据信号名称和报文id获取一个信号 */
    override fun getSignal(msgId: Int, signalName: String): S? = msgMap[CanMessage.msgIdToKey(msgId)]?.signalMap?.get(signalName)
}