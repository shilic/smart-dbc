package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartDbc.dbc.attributes.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.io.writer.allSequence
import io.github.shilic.smartGrid.core.*

/** 提供只读的 [DataBaseCan] */
interface DataBaseCan: IGridSpecificSheet, IGridRowData, IDbcElement  {
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

    // ----------------------- 自定义属性 ---------------------
    /** 自定义属性定义的集合 */
    val attributeMap : Map<String, DbcAttributeDefinition>
    /** 自定义属性值的集合 */
    val attributeValueMap: Map<String, DbcAttributeValue>

    // ------------------------ 子数据 ------------------------
    /** 消息列表，键为消息ID的16进制表示，值为消息对象 */
    val msgMap: Map<String, CanMessage>
    /** 一个消息对象，用于添加信号时使用, 保存没有依赖的信号; 不允许外部修改 */
    val independentSigMsg : CanMessage?

    // +++++++++++++++ 实现 IGridSpecificSheet 接口, 用于精确定位表格位置。 ++++++++++++++
    override val specificSheetName: String

    // +++++++++++++ 实现 IDbcElement , 用于序列化到dbc文件 ++++++++++++++
    /** 将DBC对象以一整个DBC文件字符串的方式输出； */
    @Deprecated("不推荐使用这种方式输出, 占用内存极大", ReplaceWith("allSequence"))
    override val dbcValue: String get() = allSequence.joinToString(separator = "\n")
    /** 输出DBC版本编码行，例如 VERSION "1.0.1" */
    val versionLine: String get()  = "$VERSION \"$version\""
    /** 输出节点行，例如 BU_: CCS AC */
    val nodesLine: String get() = "$BU_colon ${nodeSet.joinToString(" ")}"
    // =========================  索引器们  ===========================
    /** 根据报文标签获取报文, 键为消息ID的16进制表示 */
    operator fun get(messageTag: String): CanMessage? = msgMap[messageTag]
    /** 根据消息ID获取消息 */
    operator fun get(msgId: Int): CanMessage? = msgMap[CanMessage.msgIdToKey(msgId)]
    /** 根据报文关键字和信号名称来搜索一个信号 */
    operator fun get(messageTag: String, signalName: String): CanSignal? = getSignal(messageTag, signalName)
    /** 根据报文ID和信号名称来搜索一个信号 */
    operator fun get(msgId: Int, signalName: String): CanSignal? = getSignal(msgId, signalName)
    /** 根据索引(添加顺序)获取消息，用于在添加信号时获取刚插入的消息。超出范围返回 null */
    fun getMsgAt(index: Int): CanMessage? = msgMap.entries.elementAtOrNull(index)?.value
    /** 根据信号名称从DBC中获取一个信号; 需要遍历所有消息, 效率会比较低。 */
    fun getSignal(signalName: String): CanSignal? = msgMap.values.firstNotNullOfOrNull { message -> message.signalMap[signalName] }
    /** 根据报文id(键为消息ID的16进制表示)和信号名称获取一个信号; 相比于单使用名称，查找效率会高一些;  */
    fun getSignal(messageTag: String, signalName: String): CanSignal? = msgMap[messageTag]?.signalMap?.get(signalName)
    /** 根据报文id(键为消息ID的16进制表示)和信号名称获取一个信号; 相比于单使用名称，查找效率会高一些; 推荐使用该方法查询。 */
    fun getSignal(msgId: Int, signalName: String): CanSignal? = msgMap[CanMessage.msgIdToKey(msgId)]?.signalMap?.get(signalName)

    // ========================= 调试方法 ===============================
    /** [DataBaseCan] 基本调试信息 */
    val baseInfo: String get() = "${DataBaseCan::class.simpleName}(${::dbcTag.name}=$dbcTag, " +
            "${::version.name}=$version, ${::dbcComment.name}=$dbcComment, " +
                "nodeSet.size=${nodeSet.size}, ${::baudRate.name}=$baudRate, msgMap.size=${msgMap.size})"
    /** [DataBaseCan] 值调试信息 */
    @Suppress("UNUSED")
    val valueInfo: String get() = "${DataBaseCan::class.simpleName}($dbcTag).Values = \n${msgMap.values.map { it -> "\t${it.valueInfo}\n" }}"
}