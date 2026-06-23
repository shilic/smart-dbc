package io.github.shilic.smartDbc.can.contract

import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame

/** 抽象底层 MCU 接口
 *
 * 实现最基础的报文的收发功能
 *
 * */
interface IMcu {
    /** 本地发送 */
    fun nativeSend(canFrame: CanFrame)
    /** 本地注册监听 */
    fun nativeRegister(canListener: CanListener)
    /** 本地取消注册监听;  取消监听时, 则不需要传入其他参数了，直接取消。 */
    fun nativeUnRegister()
}