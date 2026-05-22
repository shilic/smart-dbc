package dataModel.dataEnums

import dataModel.services.IDbcElement
import io.github.shilic.smartGrid.core.*

/** 排列格式。
 *
 * [Intel] : 低位存低位, 显示 1 ;
 *
 * [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致
 *
 * [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致
 * */
enum class CANByteOrder (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override val dbcValue : String
) : IDbcElement {
    /** [Intel] 英特尔格式, 显示 1  */
    @GridColumnBind(headerText = "Intel", pattern = "Intel|INTEL|intel")
    Intel("Intel", "1"),
    /** [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致 */
    @GridColumnBind(headerText = "MotorolaMSB", pattern = "MotorolaMSB|((Motorola|motorola|MOTOROLA)\\s*(MSB|msb|Msb))|(MSB|msb|Msb)")
    MotorolaMSB("MotorolaMSB", "0") ,
    /** [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致 */
    @GridColumnBind(headerText = "MotorolaLSB", pattern = "MotorolaLSB|((Motorola|motorola|MOTOROLA)\\s*(LSB|lsb|Lsb))|(LSB|lsb|Lsb)")
    MotorolaLSB("MotorolaLSB", "0") ,
    /** 未定义 */
    CANByteOrderNotDefine("CANByteOrderNotDefine", "1");
}
