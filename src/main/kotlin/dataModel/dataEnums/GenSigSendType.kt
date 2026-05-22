package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 信号发送类型 */
enum class GenSigSendType (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override val dbcValue : String
) : IDbcElement {
    /** 周期型 */
    Cyclic ( "Cyclic", "0" ),
    /** 写入型 */
    OnWrite ( "OnWrite", "1"),
    /** 写入型(重复型) */
    OnWriteWithRepetition ( "OnWriteWithRepetition", "2"),
    /** 变化型 */
    OnChange ( "OnChange", "3"),
    /** 变化型(重复型) */
    OnChangeWithRepetition ( "OnChangeWithRepetition", "4"),
    /** 激活型 */
    IfActive ( "IfActive", "5"),
    /** 激活型(重复型) */
    IfActiveWithRepetition ( "IfActiveWithRepetition", "6"),
    /** 未定义 */
    GenSigSendTypeNotDefine ("GenSigSendTypeNotDefine", "7");
}