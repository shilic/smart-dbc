package io.github.shilic.smartDbc.can.contract

/** CAN报文监听接口 */
fun interface CanListener {
    /** 监听回调。注意：可能在子线程处理 */
    fun onListening(canId: Int, data: ByteArray)
}
