package dataModel.dataEnums

/** 帧ID类型：标准帧 0x0~0x7FF ; 扩展帧 0x0~0x1FFF_FFFF */
enum class CANMsgIdType {
    /** 标准帧 */
    Standard,
    /** 扩展帧 */
    Extended ,
    /** 未定义 */
    NotDefine;
}
