package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 排列格式。
 *
 * Intel : 低位存低位, 显示 1 ;
 *
 * Motorola : 低位存高位，显示 0 */
enum class CANByteOrder (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override val dbcValue : String
) : IDbcElement {
    /** 英特尔格式 */
    Intel("Intel", "1"),
    /** 摩托罗拉格式 MSB，与 DBC 文件格式一致 */
    MotorolaMSB("MotorolaMSB", "0") ,
    /** 摩托罗拉格式 LSB，与 CANdb++ 软件格式一致 */
    MotorolaLSB("MotorolaLSB", "0") ,
    /** 未定义 */
    CANByteOrderNotDefine("CANByteOrderNotDefine", "1");
}
