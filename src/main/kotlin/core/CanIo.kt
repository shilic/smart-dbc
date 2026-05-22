package core

/**
 * CAN IO 兼容层，兼容不同的底层CAN收发实现。<br>
 * 对外提供统一入口，内部委托给 Manager 和 McuService。
 */
class CanIo private constructor() : CanSendService {

    val manager: CanManagerService = CanManagerImp.getInstance()
    private var mcu: McuService? = null
    private var listener: CanListenService? = null

    /** 注册底层MCU适配器（通过Class反射实例化） */
    fun addAdapter(adapterClazz: Class<out McuService>): CanIo {
        mcu = adapterClazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        return this
    }

    /** 注册报文监听函数 */
    fun register(listenService: CanListenService) {
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

    companion object {
        @Volatile private var instance: CanIo? = null

        @JvmStatic fun Manager() = getInstance().manager

        fun getInstance(): CanIo = instance ?: synchronized(CanIo::class.java) {
            instance ?: CanIo().also {
                println("CanIo：兼容层初始化完成")
                instance = it
            }
        }
    }
}
