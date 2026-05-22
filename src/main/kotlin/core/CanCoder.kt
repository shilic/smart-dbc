package core

import dataModel.models.CanDbc
import dataModel.models.CanSignal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * CAN 编解码器（执行者）。用于处理报文的解码编码。<br>
 * 构造器传入DBC对象，本类只对 CanDbc 负责（低耦合）。
 */
class CanCoder(val dbc: CanDbc) {
    private val lockMap = ConcurrentHashMap<Int, ReentrantLock>()

    /** 解码报文，将接收到的CAN报文解析后存入绑定好的数据模型中 */
    fun deCode_B(canId: Int, data8: ByteArray) {
//        val msg = msgMap[canId] ?: return
//        val data64 = SLCTool.from8BytesTo64Bits(data8)
//        for (signal in msg.signalMap.values) {
//            val slice = data64.copyOfRange(signal.startBit, signal.startBit + signal.bitLength)
//            val rawValue = SLCTool.bitsToUint(slice, SLCTool.transOrder(signal.byteOrder)).toInt()
//            val phyValue = (rawValue * signal.factor) + signal.offset
//            if (!isInteger(signal.factor) || !isInteger(signal.offset) || !isInteger(phyValue))
//                signal.valid = !checkAllOnes(rawValue, signal.bitLength)
//            signal.writeValue(phyValue)
//        }
        TODO()
    }

    /** 编码数据（返回 Byte 数组） */
    fun enCode_B(canId: Int): ByteArray {
//        val msg = msgMap[canId] ?: return ByteArray(8)
//        val data64 = ByteArray(64)
//        msg.signalMap.values.forEach { sig -> writeSignalToBits64(data64, sig.readValue(), sig) }
//        return SLCTool.from64bitsTo8Bytes(data64)
        TODO()
    }

    /** 使用新的对象编码数据 */
    fun enCode_B(canId: Int, newObject: Any): ByteArray {
//        val msg = msgMap[canId] ?: return ByteArray(8)
//        val data64 = ByteArray(64)
//        msg.signalMap.values.forEach { sig -> writeSignalToBits64(data64, sig.readValue(newObject), sig) }
//        return SLCTool.from64bitsTo8Bytes(data64)
        TODO()
    }
}
