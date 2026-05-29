package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.tool.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.GridColumnBind

/** 排列格式。
 *
 * [Intel] : 低位存低位, 显示 1 ;
 *
 * [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致
 *
 * [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致
 * */
enum class CANByteOrder (
    override val intValue: Int,
    override val dbcKey : String,
    override val dbcValue : String
) : IDbcElement, IntEnum<CANByteOrder> {
    /** [Intel] 英特尔格式, 显示 1  */
    @GridColumnBind(headerText = "Intel", pattern = "Intel|INTEL|intel")
    Intel(0, "Intel", "1"),
    /** [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致 */
    @GridColumnBind(headerText = "MotorolaMSB", pattern = "MotorolaMSB|((Motorola|motorola|MOTOROLA)\\s*(MSB|msb|Msb))|(MSB|msb|Msb)")
    MotorolaMSB(1, "MotorolaMSB", "0") ,
    /** [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致 */
    @GridColumnBind(headerText = "MotorolaLSB", pattern = "MotorolaLSB|((Motorola|motorola|MOTOROLA)\\s*(LSB|lsb|Lsb))|(LSB|lsb|Lsb)")
    MotorolaLSB(2, "MotorolaLSB", "0")
}
