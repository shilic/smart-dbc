package canDemo

import io.github.shilic.smartDbc.can.binds.*

@DbcBinding([dbcTag1])
data class TestModel1 (
    @CanBinding(signalName = "CabinToCCS1_CabinTemp")
    val cabinToCCS1CabinTemp: Float = 0f,
)
