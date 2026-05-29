package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.core.services.CanManagerService
import io.github.shilic.smartDbc.can.core.services.CanSendService
import io.github.shilic.smartDbc.can.contract.IMcu

/**
 * CAN IO 兼容层，兼容不同的底层CAN收发实现。<br>
 * 对外提供统一入口，内部委托给 Manager 和 McuService。
 */
object CanIo : CanSendService {

    val manager: CanManagerService = CanManagerImp
    private var mcu: IMcu? = null
    private var listener: CanListener? = null

    /** 注册底层MCU适配器（通过Class反射实例化） */
    fun addAdapter(adapterClazz: Class<out IMcu>): CanIo {
        mcu = adapterClazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        return this
    }

    /** 注册报文监听函数 */
    fun register(listenService: CanListener) {
        listener = listenService
        mcu?.nativeRegister(listenService)
    }

    /** 取消注册所有内容 */
    fun unRegisterAll() { unRegisterCanListener(); manager.clear() }

    /** 取消注册监听事件 */
    fun unRegisterCanListener() { mcu?.let { m -> listener?.let { l -> m.nativeUnRegister(l) } } }

    // ======================================== 发送报文 ========================================

    override fun send(canId: Int, data8: ByteArray) {
        checkNotNull(mcu) { "没有注册CAN服务，无法发送报文" }
        mcu!!.nativeSend(canId, data8)
    }

    override fun send(canId: Int) {
        checkNotNull(mcu) { "没有注册CAN服务，无法发送报文" }
        mcu!!.nativeSend(canId, manager.enCode_B(canId))
    }

    override fun send(canId: Int, model: Any) {
        checkNotNull(mcu) { "没有注册CAN服务，无法发送报文" }
        mcu!!.nativeSend(canId, manager.enCode_B(canId, model))
    }
}
