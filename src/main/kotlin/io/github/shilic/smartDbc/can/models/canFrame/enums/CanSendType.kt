package io.github.shilic.smartDbc.can.models.canFrame.enums

import io.github.shilic.smartDbc.common.customComponents.IntEnum

/**
 * 发送帧类型, 只在此帧为发送帧时有意义, 默认 [CanSendType.NORMAL]  ;
 *
 * [CanSendType.NORMAL] 正常发送, [CanSendType.intValue] = 0;（发送失败会自动重发，重发最长时间为 1.5-3 秒）
 *
 * [CanSendType.SINGLE] 单次发送, [CanSendType.intValue] = 1;（只发送一次，不自动重发）
 *
 * [CanSendType.SELF_TEST] 自发自收, [CanSendType.intValue] = 2;（自测试模式，用于测试 CAN 卡是否损坏）
 *
 * [CanSendType.SINGLE_SELF_TEST] 单次自发自收, [CanSendType.intValue] = 3;（单次自测试模式，只发送一次）
 *
 * @property CanSendType.intValue 整型值
 */
enum class CanSendType (override val intValue: Int) : IntEnum<CanSendType> {
    /** [CanSendType.NORMAL] 正常发送, [CanSendType.intValue] = 0;（发送失败会自动重发，重发最长时间为 1.5-3 秒） */
    NORMAL(0),
    /** [CanSendType.SINGLE] 单次发送, [CanSendType.intValue] = 1;（只发送一次，不自动重发） */
    SINGLE(1),
    /** [CanSendType.SELF_TEST] 自发自收, [CanSendType.intValue] = 2;（自测试模式，用于测试 CAN 卡是否损坏） */
    SELF_TEST(2),
    /** [CanSendType.SINGLE_SELF_TEST] 单次自发自收, [CanSendType.intValue] = 3;（单次自测试模式，只发送一次） */
    SINGLE_SELF_TEST(3);
}