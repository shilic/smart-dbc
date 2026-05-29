package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.numberUtils.toHexStr
import io.github.shilic.smartGrid.core.*

/** 提供不可变的 CanDbc */
interface CanDbc: IGridSpecificSheet, IGridRowData, IDbcElement  {
    // +++++++++++++ IGridRowData 接口实现 +++++++++++++++
    override val gridKey: String get() = dbcTag
    override val dbcKey: String get() = dbcTag

    // ------------------------- 基本信息 ---------------------
    /** DBC 标签 */
    val dbcTag: String
    /** DBC 版本 */
    val version: String
    /** DBC 描述 */
    val dbcComment: String
    /** 节点列表 */
    val nodeSet: Set<String>
    /** 波特率 (单位：kbps) */
    val baudRate: Int

    // ------------------------ 子数据 ------------------------
    /** 消息列表，键为消息ID的16进制表示，值为消息对象 */
    val msgMap: Map<String, CanMessage>

    // +++++++++++++++ 实现 IGridSpecificSheet 接口, 用于精确定位表格位置。 ++++++++++++++
    override val specificSheetName: String

    // +++++++++++++ 实现 IDbcElement , 用于序列化到dbc文件 ++++++++++++++
    override val dbcValue: String get() = buildString {
        TODO("实现 IDbcElement , 用于序列化到dbc文件 ")
    }
    // =========================  索引器们  ===========================
    /** 根据报文标签获取报文 */
    operator fun get(messageTag: String): CanMessage? = msgMap[messageTag]
    /** 根据消息ID获取消息 */
    operator fun get(msgId: Int): CanMessage? = msgMap[msgId.toHexStr()]
    /** 根据索引(添加顺序)获取消息，用于在添加信号时获取刚插入的消息。超出范围返回 null */
    fun getMsgAt(index: Int): CanMessage? = msgMap.entries.elementAtOrNull(index)?.value
    /** 根据信号名称获取一个信号（遍历所有消息） */
    fun getSignal(signalTag: String): CanSignal? = msgMap.values.firstNotNullOfOrNull { message -> message.signalMap[signalTag] }
    /** 根据信号名称和报文id获取一个信号 */
    fun getSignal(messageTag: String, signalTag: String): CanSignal? = msgMap[messageTag]?.signalMap?.get(signalTag)
    /** 根据信号名称和报文id获取一个信号 */
    fun getSignal(msgId: Int, signalTag: String): CanSignal? = msgMap[msgId.toHexStr()]?.signalMap?.get(signalTag)

    // ========================= 调试方法 ===============================
    /** 基本信息 */

    val baseInfo: String get() = "CanDbcImpBaseInfo(dbcTag=$dbcTag, version = $version, dbcComment=$dbcComment, " +
                "nodeSet.size=${nodeSet.size}, baudRate=$baudRate, msgMap.size=${msgMap.size})"
    @Suppress("UNUSED")
    val valueInfo: String get() = "CanDbc($dbcTag).Values = \n${msgMap.values.map { it -> "\t${it.valueInfo}\n" }}"
}