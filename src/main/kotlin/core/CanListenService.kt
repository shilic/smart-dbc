package core

/** CAN报文监听接口 */
fun interface CanListenService {
    /** 监听回调。注意：可能在子线程处理 */
    fun listened(canId: Int, data8: ByteArray)
}
