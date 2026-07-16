package canTest

import io.github.shilic.smartDbc.can.models.canFrame.models.*
import io.github.shilic.smartDbc.can.models.canFrame.enums.*
import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.CanExternFlag
import kotlin.test.Test
import kotlin.test.*

class CanFrameDataTest {

    @Test
    fun `CanFrameData empty creates zero-filled frame`() {
        val frame = CanFrameData.empty(0x100)
        assertEquals(0x100, frame.msgId)
        assertEquals(8, frame.dataLen)
        assertTrue(frame.data.all { it == 0.toByte() })
    }

    @Test
    fun `CanFrameData empty with custom length`() {
        val frame = CanFrameData.empty(0x200, 4)
        assertEquals(4, frame.data.size)
    }

    @Test
    fun `CanFrameData interface mapping`() {
        val frame = CanFrameData(
            canMsgId = 0x18ABAB01,
            canData = byteArrayOf(7, 8, 9),
            canSendType = CanSendType.SINGLE,
            canRemoteFlag = CanRemoteFlag.REMOTE_FRAME,
            canExternFlag = CanExternFlag.Standard,
            canFdFlag = CanFdFlag.CanFd
        )
        assertEquals(0x18ABAB01, frame.msgId)
        assertEquals(3, frame.dataLen)
        assertEquals(CanSendType.SINGLE.intValue, frame.sendType)
        assertEquals(CanRemoteFlag.REMOTE_FRAME.intValue, frame.remoteFlag)
        assertEquals(CanExternFlag.Standard.intValue, frame.externFlag)
        assertEquals(CanFdFlag.CanFd.intValue, frame.fdFlag)
    }

    @Test
    fun `CanFrameData defaults`() {
        val frame = CanFrameData(0x100, ByteArray(4))
        assertEquals(CanSendType.NORMAL.intValue, frame.sendType)
        assertEquals(CanRemoteFlag.DATA_FRAME.intValue, frame.remoteFlag)
        assertEquals(CanExternFlag.Extended.intValue, frame.externFlag)
        assertEquals(CanFdFlag.Can.intValue, frame.fdFlag)
    }

    @Test
    fun `CanFrameData equals identical frames`() {
        val a = CanFrameData(0x100, byteArrayOf(1, 2, 3))
        val b = CanFrameData(0x100, byteArrayOf(1, 2, 3))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `CanFrameData equals different data not equal`() {
        val a = CanFrameData(0x100, byteArrayOf(1, 2, 3))
        val b = CanFrameData(0x100, byteArrayOf(1, 2, 4))
        assertNotEquals(a, b)
    }

    @Test
    fun `CanFrameData equals different msgId not equal`() {
        val a = CanFrameData(0x100, byteArrayOf(1, 2))
        val b = CanFrameData(0x101, byteArrayOf(1, 2))
        assertNotEquals(a, b)
    }

    @Test
    fun `CanFrameData equals different sendType not equal`() {
        val a = CanFrameData(0x100, byteArrayOf(1), canSendType = CanSendType.NORMAL)
        val b = CanFrameData(0x100, byteArrayOf(1), canSendType = CanSendType.SINGLE)
        assertNotEquals(a, b)
    }

    @Test
    fun `ByteArray toCanFrameData extension`() {
        val data = byteArrayOf(0x0A, 0x0B)
        val frame = data.toCanFrameData(0x7FF)
        assertEquals(0x7FF, frame.msgId)
        assertTrue(data.contentEquals(frame.data))
    }

    // ==================== IntEnum for CanFD/Remote/SendType ====================

    @Test
    fun `CanFdFlag fromInt`() {
        assertEquals(CanFdFlag.Can, IntEnum.fromInt<CanFdFlag>(0))
        assertEquals(CanFdFlag.CanFd, IntEnum.fromInt<CanFdFlag>(1))
    }

    @Test
    fun `CanRemoteFlag fromInt`() {
        assertEquals(CanRemoteFlag.DATA_FRAME, IntEnum.fromInt<CanRemoteFlag>(0))
        assertEquals(CanRemoteFlag.REMOTE_FRAME, IntEnum.fromInt<CanRemoteFlag>(1))
    }

    @Test
    fun `CanSendType fromInt`() {
        assertEquals(CanSendType.NORMAL, IntEnum.fromInt<CanSendType>(0))
        assertEquals(CanSendType.SINGLE, IntEnum.fromInt<CanSendType>(1))
        assertEquals(CanSendType.SELF_TEST, IntEnum.fromInt<CanSendType>(2))
        assertEquals(CanSendType.SINGLE_SELF_TEST, IntEnum.fromInt<CanSendType>(3))
    }
}
