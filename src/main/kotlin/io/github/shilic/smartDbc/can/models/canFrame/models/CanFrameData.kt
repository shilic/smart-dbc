package io.github.shilic.smartDbc.can.models.canFrame.models

import io.github.shilic.smartDbc.can.models.canFrame.enums.*
import io.github.shilic.smartDbc.can.models.canFrame.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*

/**
 * CAN帧接口
 *
 * @property canMsgId 报文ID
 * @property canData 报文数据
 * @property canSendType 发送帧类型, 只在此帧为发送帧时有意义。;
 * @property canRemoteFlag 远程帧标志, 默认0 数据帧;
 * @property canExternFlag 扩展帧标志
 * @property canFdFlag CanFd 标志位
 */
data class CanFrameData (
    /** 报文ID */
    val canMsgId: Int,
    /** 报文数据 */
    val canData: ByteArray,
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
    val canSendType: CanSendType = CanSendType.NORMAL,
    /**
     * 远程帧标志, 默认0 数据帧;
     *
     *  [CanRemoteFlag.DATA_FRAME] 数据帧, [CanRemoteFlag.intValue] = 0 ;
     *
     *  [CanRemoteFlag.REMOTE_FRAME] 远程帧, [CanRemoteFlag.intValue] = 1 ;
     *
     * @property CanRemoteFlag.intValue 整型值
     */
    val canRemoteFlag: CanRemoteFlag = CanRemoteFlag.DATA_FRAME,
    /** 扩展帧标志
     *
     *  [CanExternFlag.Standard] 标准帧， [CanExternFlag.intValue] = 0 ；范围： 0x0~0x7FF ; 在DBC文件中 = msgId ;
     *
     *  [CanExternFlag.Extended] 扩展帧， [CanExternFlag.intValue] = 1 ；范围：0x0~0x1FFF_FFFF ; 在DBC文件中 = msgId + 0x8000_0000L ;
     *
     *  @property CanExternFlag.intValue 整型值
     * */
    val canExternFlag: CanExternFlag = CanExternFlag.Extended,
    /**
     *  CanFd 标志位
     *
     *  [CanFdFlag.Can] 普通CAN总线, [CanFdFlag.intValue] = 0
     *
     *  [CanFdFlag.CanFd] CanFd, [CanFdFlag.intValue] = 1
     *
     * @property CanFdFlag.intValue 整型值
     */
    val canFdFlag: CanFdFlag = CanFdFlag.Can
) : CanFrame {
    // ++++++++++++++++++++++++ 实现 CanFrame 接口 +++++++++++++++++++++++++
    override val msgId: Int get() = canMsgId
    override val data: ByteArray get() = canData
    override val sendType: Int get() = canSendType.intValue
    override val remoteFlag: Int get() = canRemoteFlag.intValue
    override val externFlag: Int get() = canExternFlag.intValue
    override val fdFlag: Int get() = canFdFlag.intValue
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanFrameData

        if (canMsgId != other.canMsgId) return false
        if (!canData.contentEquals(other.canData)) return false
        if (canSendType != other.canSendType) return false
        if (canRemoteFlag != other.canRemoteFlag) return false
        if (canExternFlag != other.canExternFlag) return false
        if (canFdFlag != other.canFdFlag) return false
        if (dataLen != other.dataLen) return false

        return true
    }
    override fun hashCode(): Int {
        var result = canMsgId
        result = 31 * result + canData.contentHashCode()
        result = 31 * result + canSendType.hashCode()
        result = 31 * result + canRemoteFlag.hashCode()
        result = 31 * result + canExternFlag.hashCode()
        result = 31 * result + canFdFlag.hashCode()
        result = 31 * result + dataLen
        return result
    }

    override fun toString(): String = display
    companion object {
        fun empty(canMsgId: Int) = CanFrameData(canMsgId, ByteArray(8))
    }
}
/** 使用扩展函数，快速创建一个 CanFrameData  */
fun ByteArray.toCanFrame(canMsgId: Int) : CanFrameData = CanFrameData(canMsgId, this)