package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 报文发送类型 */
enum class GenMsgSendType (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override val dbcValue : String
) : IDbcElement {
    /** 周期型 */
    Cycle ("Cycle", "0"),
    /** 事件型 */
    Event ("Event", "1"),
    /** 激活型 */
    IfActive ("IfActive", "2"),
    /** 持续型 */
    CE ("CE", "3"),
    /** 持续型(激活型) */
    CA ("CA", "4"),
    /** 未定义 */
    GenMsgSendTypeNotDefine ("GenMsgSendTypeNotDefine", "5");
}
