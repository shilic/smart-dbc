package canTest

import io.github.shilic.smartDbc.can.core.DefaultMcuAdapter
import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.can.models.canFrame.models.CanFrameData
import kotlin.test.Test
import kotlin.test.*

class DefaultMcuAdapterTest {

    private val adapter = DefaultMcuAdapter
    private val dummyFrame: CanFrame = CanFrameData(0x100, ByteArray(4))
    private val dummyListener = object : CanListener {
        override val listenerName = "test"
        override fun onListening(canFrame: CanFrame) {}
    }

    @Test
    fun `transmit does not throw`() {
        adapter.transmit(dummyFrame)
    }

    @Test
    fun `register does not throw`() {
        adapter.register(dummyListener)
    }

    @Test
    fun `unRegister does not throw`() {
        adapter.unRegister(dummyListener)
    }

    @Test
    fun `unRegisterAll does not throw`() {
        adapter.unRegisterAll()
    }
}
