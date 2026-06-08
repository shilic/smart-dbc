package io.github.shilic.smartDbc.can.models.canFrame.enums

import io.github.shilic.smartDbc.common.customComponents.IntEnum

/**
 * 远程帧标志, 默认0 数据帧;
 *
 *  [CanRemoteFlag.DATA_FRAME] 数据帧, [CanRemoteFlag.intValue] = 0 ;
 *
 *  [CanRemoteFlag.REMOTE_FRAME] 远程帧, [CanRemoteFlag.intValue] = 1 ;
 *
 * @property CanRemoteFlag.intValue 整型值
 */
enum class CanRemoteFlag(override val intValue: Int) : IntEnum<CanRemoteFlag> {
    /** [CanRemoteFlag.DATA_FRAME] 数据帧, [CanRemoteFlag.intValue] = 0 */
    DATA_FRAME(0),
    /** [CanRemoteFlag.REMOTE_FRAME] 远程帧, [CanRemoteFlag.intValue] = 1 */
    REMOTE_FRAME(1)
}