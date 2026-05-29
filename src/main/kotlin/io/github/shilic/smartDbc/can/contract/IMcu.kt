package io.github.shilic.smartDbc.can.contract

import io.github.shilic.smartDbc.can.models.canFrame.interfaces.CanFrame

/** 抽象底层 MCU 接口
 *
 * */
interface IMcu {
    fun nativeSend(canId: Int, data8: ByteArray)
    fun nativeRegister(canListener: CanListener)
    fun nativeUnRegister(canListener: CanListener)
    fun nativeReceive(): Array<CanFrame> = error("该方法暂时未实现")
}