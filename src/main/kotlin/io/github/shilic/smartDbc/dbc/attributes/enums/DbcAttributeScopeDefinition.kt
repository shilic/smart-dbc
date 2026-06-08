package io.github.shilic.smartDbc.dbc.attributes.enums

import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*

/** 自定义属性的作用域类型
 *
 * [DbcAttributeScopeDefinition.Net] 网络类型, 在DBC文件中的编码为 空字符串 "" , 表示整个DBC文件的自定义属性;
 *
 * [DbcAttributeScopeDefinition.Message] 报文类型, 在DBC文件中的编码为 BO_
 *
 * [DbcAttributeScopeDefinition.Signal] 信号类型, 在DBC文件中的编码为 SG_
 *
 * [DbcAttributeScopeDefinition.Node] 节点类型, 在DBC文件中的编码为 BU_
 * */
enum class DbcAttributeScopeDefinition(
    override val dbcKey : String,
    override val dbcValue : String
) : IDbcElement {
    /** [DbcAttributeScopeDefinition.Net] 网络类型, 在DBC文件中的编码为 空字符串 "" , 表示整个DBC文件的自定义属性;
     *
     * 例如 DB 的名称 DBName ; 例如 总线类型 BusType ; */
    Net(Network, ""),
    /** [DbcAttributeScopeDefinition.Message] 报文类型, 在DBC文件中的编码为 BO_ */
    Message(BO_, BO_),
    /** [DbcAttributeScopeDefinition.Signal] 信号类型, 在DBC文件中的编码为 SG_ */
    Signal(SG_, SG_),
    /** [DbcAttributeScopeDefinition.Node] 节点类型, 在DBC文件中的编码为 BU_ */
    Node(BU_, BU_),

    /* 以下4个作用域不常用; 只有 Net 、Message、Signal和Node最常用。
    * 另外, 我在can db ++ 软件中，定义了以下自定义属性后; 也没有找到对应的地方去添加该自定义属性;
    * 故, 我们暂且不使用下边的不常用作用域, 只使用上边的4个常见作用域。 */
    /** 环境变量 */
    EnvVariable(EV_, EV_),
    /** 控制单元环境变量 */
    EcuEnvVariable(BU_EV_REL_, BU_EV_REL_),
    /** 节点 - 传输报文 */
    NodeTxMessage(BU_BO_REL_, BU_BO_REL_),
    /** 节点 - 接收信号 */
    NodeRxSignal(BU_SG_REL_, BU_SG_REL_);

    companion object {
        /** 根据字符串创建属性作用域类型
         *
         *  "" -> [Net]
         *
         *  [EV_] -> [EnvVariable]
         *
         *  [BO_] -> [Message]
         *
         *  [SG_] -> [Signal]
         *
         *  [BU_] -> [Node]
         *
         *  [BU_EV_REL_] -> [EcuEnvVariable]
         *
         *  [BU_BO_REL_] -> [NodeTxMessage]
         *
         *  [BU_SG_REL_] -> [NodeRxSignal]
         *  */
        fun createBy(value: String) :DbcAttributeScopeDefinition = when(value) {
            "" -> Net
            BO_ -> Message
            SG_ -> Signal
            BU_ -> Node

            EV_ -> EnvVariable
            BU_EV_REL_ -> EcuEnvVariable
            BU_BO_REL_ -> NodeTxMessage
            BU_SG_REL_ -> NodeRxSignal
            else -> throw Exception("根据字符串创建 '${DbcAttributeScopeDefinition::class.simpleName}'枚举失败， 输入值: $value")
        }
    }
}