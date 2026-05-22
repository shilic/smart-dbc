package dataModel.dataEnums

/** 数据类型，默认无符号 */
enum class CANDataType {
    /** 无符号 */
    Unsigned,
    /** 有符号 */
    Signed,
    /** 浮点数 */
    Float,
    /** 双精度浮点数 */
    Double,
    /** 布尔 */
    Bool,
    /** 未定义 */
    NotDefine;
}
