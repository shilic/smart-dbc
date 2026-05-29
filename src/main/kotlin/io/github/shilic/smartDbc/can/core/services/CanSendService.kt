package io.github.shilic.smartDbc.can.core.services

/** CAN发送服务 */
interface CanSendService {
    fun send(canId: Int, data8: ByteArray)
    fun send(canId: Int)
    fun send(canId: Int, model: Any)
}
