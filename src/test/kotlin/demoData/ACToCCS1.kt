package demoData

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.CanCopyable

@DbcBinding([dbcTag1])
data class ACToCCS1 (
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_FactoryID")
    var factoryID: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_CabinTemp")
    var cabinTemp: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_ColdGearSts")
    var coldGearSts: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_FanGearSts")
    var fanGearSts: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_FanMotFlt")
    var fanMotFlt: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_ColdMotFlt")
    var coldMotFlt: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_AirSts")
    var airSts: Int = 0,
    @CanBinding(ACToCCS1_Id, "CabinToCCS1_RollCnt")
    var rollCnt: Int = 0,
): CanCopyable<ACToCCS1> {
    override fun copyNew(): ACToCCS1 {
        return this.copy()
    }
}
