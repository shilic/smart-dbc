package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 帧ID类型：标准帧 0x0~0x7FF ; 扩展帧 0x0~0x1FFF_FFFF */
enum class CanMsgIdType  (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override  val dbcValue : String
) : IDbcElement {
    /** 标准帧 */
    Standard ("Standard", ""),
    /** 扩展帧 */
    Extended ("Extended", ""),
    /** 未定义 */
    CanMsgIdTypeNotDefine("CanMsgIdTypeNotDefine", "");
}
