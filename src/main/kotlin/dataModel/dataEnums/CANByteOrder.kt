package dataModel.dataEnums

/** 排列格式。Intel:低位存低位; Motorola:低位存高位 */
enum class CANByteOrder {
    /** 英特尔格式 */
    Intel,
    /** 摩托罗拉格式 MSB，与 DBC 文件格式一致 */
    MotorolaMSB,
    /** 摩托罗拉格式 LSB，与 CANdb++ 软件格式一致 */
    MotorolaLSB,
    /** 未定义 */
    NotDefine;
}
