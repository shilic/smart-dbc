package canTest

import demoData.ExampleDbcPath1
import demoData.msg1_Id
import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.core.CanIo
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import io.github.shilic.smartDbc.valueConverter.encodeCanFrame
import kotlin.test.Test
import kotlin.test.*

// Test models with annotations
@DbcBinding(dbcTags = ["Example"])
data class TestBindModel(
    @CanBinding(msgId = 0x18ABAB01, signalName = "msg1_sig1")
    var sig1: Int = 0,
    @CanBinding(msgId = 0x18ABAB01, signalName = "msg1_sig2")
    var sig2: Int = 0
)

@CanMessageBinding(dbcTag = "Example", msgId = 0x18ABAB01)
data class TestMsgBindModel(
    @CanSignalBinding(signalName = "msg1_sig1")
    var sig1: Int = 0
)

class CanIoTest {

    @Test
    fun `findMessage returns message from registered DBC`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        val msg = CanIo.findMessage(msg1_Id)
        assertNotNull(msg)
        assertEquals("message1", msg.msgName)
    }

    @Test
    fun `findMessage returns null for unknown msgId`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        assertNull(CanIo.findMessage(0xDEAD))
    }

    @Test
    fun `findSignal with msgId returns correct signal`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        val sig = CanIo.findSignal(CanBinding(msgId = msg1_Id, signalName = "msg1_sig1"))
        assertNotNull(sig)
        assertEquals("msg1_sig1", sig.signalName)
    }

    @Test
    fun `findSignal without msgId falls back to scan`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        val sig = CanIo.findSignal(CanBinding(msgId = CanBinding.DEFAULT_ID, signalName = "msg1_sig1"))
        assertNotNull(sig)
        assertEquals("msg1_sig1", sig.signalName)
    }

    @Test
    fun `bind with DbcBinding annotation`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        CanIo.modelMap.clear()
        val model = TestBindModel()
        CanIo.bind(model)

        val retrieved: TestBindModel? = CanIo.getModel()
        assertNotNull(retrieved)
        // Verify signal was bound
        val sig = CanIo.findSignal(CanBinding(msgId = msg1_Id, signalName = "msg1_sig1"))
        assertNotNull(sig)
        assertEquals(TestBindModel::class, sig.originalOwnerType)
    }

    @Test
    fun `binding with CanMessageBinding annotation`() {
        CanIo.dbcMap.clear()
        CanIo.dbcMap["Example"] = DbcFileReader(ExampleDbcPath1).read()
        CanIo.modelMap.clear()
        val model = TestMsgBindModel()
        CanIo.binding(model)

        val retrieved: TestMsgBindModel? = CanIo.getModel()
        assertNotNull(retrieved)
    }

    @Test
    fun `bind throws when DBC not registered`() {
        CanIo.dbcMap.clear()
        CanIo.modelMap.clear()
        val model = TestBindModel()
        val ex = assertFailsWith<IllegalArgumentException> { CanIo.bind(model) }
        assertTrue(ex.message!!.contains("没有提前在"))
    }

    @Test
    fun `bind throws when annotation missing`() {
        CanIo.dbcMap.clear()
        CanIo.modelMap.clear()
        data class NoAnnotation(val x: Int = 0)
        val ex = assertFailsWith<IllegalStateException> { CanIo.bind(NoAnnotation()) }
        assertTrue(ex.message!!.contains("需要标记"))
    }

    @Test
    fun `encodeCanFrame via CanIo transmit`() {
        CanIo.dbcMap.clear()
        val dbc = DbcFileReader(ExampleDbcPath1).read()
        CanIo.dbcMap["Example"] = dbc
        // Set known signal values
        dbc[msg1_Id, "msg1_sig1"]?.currentPhyValue = 42.0
        dbc[msg1_Id, "msg1_sig2"]?.currentPhyValue = 16.0

        // findMessage + encodeCanFrame should work
        val msg = CanIo.findMessage(msg1_Id)
        assertNotNull(msg)
        val frame = msg.encodeCanFrame()
        assertEquals(msg1_Id, frame.msgId)
    }

    @Test
    fun `decodeCanFrame via CanIo`() {
        CanIo.dbcMap.clear()
        val dbc = DbcFileReader(ExampleDbcPath1).read()
        CanIo.dbcMap["Example"] = dbc

        val frame = io.github.shilic.smartDbc.can.models.canFrame.models.CanFrameData(
            msg1_Id, byteArrayOf(42, 16, 0, 0, 0, 0, 0, 0)
        )
        CanIo.decodeCanFrame(frame)
        assertEquals(42.0, dbc[msg1_Id, "msg1_sig1"]?.currentPhyValue)
        assertEquals(16.0, dbc[msg1_Id, "msg1_sig2"]?.currentPhyValue)
    }
}
