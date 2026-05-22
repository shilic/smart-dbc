package core

/** CAN数据类，将报文ID和数据封装成一个整体 */
data class CanFrameData(val msgId: Int, val bytes8: ByteArray) {
    override fun equals(other: Any?): Boolean =
        other is CanFrameData && msgId == other.msgId && bytes8.contentEquals(other.bytes8)
    override fun hashCode(): Int = 31 * msgId + bytes8.contentHashCode()
}
