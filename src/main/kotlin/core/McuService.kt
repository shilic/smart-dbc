package core

/** 抽象底层 CAN 收发实现 */
interface McuService {
    fun nativeSend(canId: Int, data8: ByteArray)
    fun nativeRegister(canListener: CanListenService)
    fun nativeUnRegister(canListener: CanListenService)
    fun nativeReceive(): Array<CanFrameData> = error("该方法暂时未实现")
}
