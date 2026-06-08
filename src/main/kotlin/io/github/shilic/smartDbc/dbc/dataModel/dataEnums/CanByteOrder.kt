package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.GridColumnBind

const val ONE = "1"
const val ZERO = "0"

/** 排列格式。
 *
 * [Intel] : 低位存低位, 显示 1 ;
 *
 * [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致
 *
 * [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致
 * */
enum class CanByteOrder (
    override val intValue: Int,
    override val dbcKey : String,
    override val dbcValue : String
): IDbcElement, IntEnum<CanByteOrder> {
    /** [Intel] 英特尔格式, 显示 1  */
    @GridColumnBind(headerText = "Intel", pattern = "Intel|INTEL|intel")
    Intel(0, "Intel", ONE),
    /** [MotorolaMSB] 摩托罗拉格式 MSB, 显示 0 , 与 DBC 文件格式一致 */
    @GridColumnBind(headerText = "MotorolaMSB", pattern = "MotorolaMSB|((Motorola|motorola|MOTOROLA)\\s*(MSB|msb|Msb))|(MSB|msb|Msb)")
    MotorolaMSB(1, "MotorolaMSB", ZERO) ,
    /** [MotorolaLSB] 摩托罗拉格式 LSB, 显示 0 , 与 CANdb++ 软件格式一致 */
    @GridColumnBind(headerText = "MotorolaLSB", pattern = "MotorolaLSB|((Motorola|motorola|MOTOROLA)\\s*(LSB|lsb|Lsb))|(LSB|lsb|Lsb)")
    MotorolaLSB(2, "MotorolaLSB", ZERO);
    companion object {
        fun createBy(dbcValue: String) = when(dbcValue) {
            ONE -> Intel
            ZERO -> MotorolaMSB
            else -> error("数值 0 表示MotorolaMSB, 数值 1 表示Intel, 其余的均为非法值，无法转换为排列格式")
        }
    }
}
