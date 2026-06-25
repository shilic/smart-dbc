package io.github.shilic.smartDbc.can.contract

import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame

/** 抽象底层 MCU 接口
 *
 * 实现最基础的报文的收发功能
 *
 * */
interface IMcu {
    /** 传输CAN报文 */
    fun transmit(canFrame: CanFrame)
    /** 注册监听函数 */
    fun register(canListener: CanListener)
    /** 取消所有的注册监听; */
    fun unRegisterAll()
}