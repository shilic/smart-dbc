package dataModel.dataEnums

/** 报文发送类型 */
enum class MsgSendType {
    /** 周期型 */
    Cycle,
    /** 事件型 */
    Event,
    /** 激活型 */
    IfActive,
    /** 持续型 */
    CE,
    /** 持续型(激活型) */
    CA,
    /** 未定义 */
    NotDefine;
}
