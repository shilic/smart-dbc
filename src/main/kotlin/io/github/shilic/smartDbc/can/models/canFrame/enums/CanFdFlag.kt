package io.github.shilic.smartDbc.can.models.canFrame.enums

import io.github.shilic.smartDbc.common.customComponents.IntEnum

/**
 * CanFd 标志位
 *
 *  [CanFdFlag.Can] 普通CAN总线, [CanFdFlag.intValue] = 0
 *
 *  [CanFdFlag.CanFd] CanFd, [CanFdFlag.intValue] = 1
 *
 * @property CanFdFlag.intValue 整型值
 */
enum class CanFdFlag (override val intValue: Int) : IntEnum<CanFdFlag> {
    /** [CanFdFlag.Can] 普通CAN总线, [CanFdFlag.intValue] = 0 */
    Can(0),
    /** [CanFdFlag.CanFd] CanFd, [CanFdFlag.intValue] = 1 */
    CanFd(1)
}