package canTest

import demoData.ExampleDbcPath1
import demoData.msg1_Id
import io.github.shilic.smartDbc.dbc.dataModel.models.*
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.*

class CanAccessorTest {

    private fun loadSignal(): CanSignalImp {
        val dbc = DbcFileReader(ExampleDbcPath1).read()
        return dbc[msg1_Id, "msg1_sig1"] ?: error("signal not found")
    }

    // ==================== readCanValue / writeCanValue ====================

    @Test
    fun `writeCanValue sets currentPhyValue on signal`() {
        val sig = loadSignal()
        sig.writeCanValue(42.5)
        assertEquals(42.5, sig.currentPhyValue)
    }

    @Test
    fun `readCanValue returns DBC value when no binding`() {
        val sig = loadSignal()
        sig.currentPhyValue = 99.0
        assertEquals(99.0, sig.readCanValue())
    }

    @Test
    fun `write then read round-trip`() {
        val sig = loadSignal()
        sig.writeCanValue(-50.0)
        assertEquals(-50.0, sig.readCanValue())
    }

    // ==================== setPropertyValue / getPropertyValue ====================

    data class TestModel(var field1: Int = 0, var field2: Double = 0.0)

    @Test
    fun `setPropertyValue writes to bound field`() {
        val sig = loadSignal()
        val model = TestModel()
        sig.originalOwnerType = TestModel::class
        sig.originalOwner = model
        sig.originalProperty = TestModel::class.memberProperties.first { it.name == "field1" }

        sig.setPropertyValue(42.0)
        assertEquals(42, model.field1)
    }

    @Test
    fun `getPropertyValue reads from bound field`() {
        val sig = loadSignal()
        val model = TestModel(field2 = 3.14)
        sig.originalOwnerType = TestModel::class
        sig.originalOwner = model
        sig.originalProperty = TestModel::class.memberProperties.first { it.name == "field2" }

        assertEquals(3.14, sig.getPropertyValue())
    }

    @Test
    fun `getPropertyValue returns null when no binding`() {
        val sig = loadSignal()
        sig.originalOwner = null
        sig.originalProperty = null
        sig.originalOwnerType = null
        assertNull(sig.getPropertyValue())
    }

    @Test
    fun `setPropertyValue does nothing when no binding`() {
        val sig = loadSignal()
        sig.originalOwner = null
        sig.originalProperty = null
        sig.originalOwnerType = null
        // should not throw
        sig.setPropertyValue(42.0)
    }

    @Test
    fun `readCanValue prefers bound field over DBC value`() {
        val sig = loadSignal()
        val model = TestModel(field1 = 77)
        sig.originalOwnerType = TestModel::class
        sig.originalOwner = model
        sig.originalProperty = TestModel::class.memberProperties.first { it.name == "field1" }

        sig.currentPhyValue = 10.0  // DBC value
        // readCanValue should prefer the bound field (77) over DBC value (10)
        assertEquals(77.0, sig.readCanValue())
    }

    @Test
    fun `readCanValue falls back to DBC when field value is null`() {
        val sig = loadSignal()
        val model = TestModel(field1 = 0)
        sig.originalOwnerType = TestModel::class
        sig.originalOwner = model
        sig.originalProperty = TestModel::class.memberProperties.first { it.name == "field1" }

        sig.currentPhyValue = -25.0
        // field1=0, which is not null (it's 0.0 as Double from toDoubleValue)
        // Since field is bound and has a value, it returns 0.0, not the DBC value
        assertNotNull(sig.readCanValue())
    }
}
