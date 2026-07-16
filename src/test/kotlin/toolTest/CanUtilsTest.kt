package toolTest

import io.github.shilic.smartDbc.common.tool.dataOrder
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.CanByteOrder
import io.github.shilic.numberUtils.DataType
import kotlin.test.Test
import kotlin.test.*

class CanUtilsTest {

    @Test
    fun `dataOrder Intel maps to Intel`() {
        assertEquals(DataType.Intel, CanByteOrder.Intel.dataOrder)
    }

    @Test
    fun `dataOrder MotorolaMSB maps to Motorola`() {
        assertEquals(DataType.Motorola, CanByteOrder.MotorolaMSB.dataOrder)
    }

    @Test
    fun `dataOrder MotorolaLSB maps to Motorola`() {
        assertEquals(DataType.Motorola, CanByteOrder.MotorolaLSB.dataOrder)
    }
}
