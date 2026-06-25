package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.contract.IMcu
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame

/**
 * 默认的MCU适配器，当用户没有注册MCU适配器时，使用这个适配器
 */
object DefaultMcuAdapter: IMcu {
    val logTag: String = "${DefaultMcuAdapter::class.simpleName}"

    override fun transmit(canFrame: CanFrame) {
        println("$logTag: 发送报文失败, MCU适配器未实现, 请在CanIo中注册MCU适配器")
    }
    override fun register(canListener: CanListener) {
        println("$logTag: 注册监听器失败, MCU适配器未实现, 请在CanIo中注册MCU适配器")
    }
    override fun unRegisterAll() {
        println("$logTag: 取消注册监听器失败, MCU适配器未实现, 请在CanIo中注册MCU适配器")
    }
}