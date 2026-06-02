package io.github.shilic.smartDbc.can.contract

import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame

/** CAN报文监听器接口 */
interface CanListener {
    /** 监听器名称, 子类必须实现 */
    val listenerName: String
    /** 监听回调; 当监听事件触发时，会调用此方法；
     * @param canFrame 监听到的报文
     * */
    fun onListening(canFrame: CanFrame)
}
