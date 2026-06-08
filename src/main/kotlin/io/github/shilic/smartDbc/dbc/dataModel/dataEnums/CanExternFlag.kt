package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.IDbcElement

/** 扩展帧标志
 *
 *
 *  [CanExternFlag.Standard] 标准帧， [CanExternFlag.intValue] = 0 ；范围： 0x0~0x7FF ; 在DBC文件中 = msgId ;
 *
 * [CanExternFlag.Extended] 扩展帧， [CanExternFlag.intValue] = 1 ；范围：0x0~0x1FFF_FFFF ; 在DBC文件中 = msgId + 0x8000_0000L ;
 *
 *  @property CanExternFlag.intValue 整型值
 * */
enum class CanExternFlag (
    override val intValue: Int,
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override  val dbcValue : String
) : IDbcElement, IntEnum<CanExternFlag>{
    /** [CanExternFlag.Standard] 标准帧， [CanExternFlag.intValue] = 0 ；范围： 0x0~0x7FF ; 在DBC文件中 = msgId ;  */
    Standard (0, "Standard", ""),
    /** [CanExternFlag.Extended] 扩展帧， [CanExternFlag.intValue] = 1 ；范围：0x0~0x1FFF_FFFF ; 在DBC文件中 = msgId + 0x8000_0000L ; */
    Extended (1, "Extended", "");
    companion object {
        /** 创建扩展帧标志 */
        fun createByLongIdCode(longIdCode : Long ) = if (longIdCode >= 0x8000_0000) Extended else Standard
    }
}
