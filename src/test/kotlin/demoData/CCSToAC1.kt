package demoData

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.CanCopyable

@DbcBinding([dbcTag1])
@CanMessageBinding(dbcTag1, CCSToAC1_Id)
data class CCSToAC1 (
    @CanBinding(CCSToAC1_Id, "CCSToAC1_FactoryID")
    @CanSignalBinding("CCSToAC1_FactoryID")
    var factoryID: Int = 0,
    @CanBinding(CCSToAC1_Id, "CCSToAC1_AirSw")
    @CanSignalBinding("CCSToAC1_AirSw")
    var airSw: Int = 0,
    @CanBinding(CCSToAC1_Id, "CCSToCabin1_ColdGearReq")
    @CanSignalBinding("CCSToCabin1_ColdGearReq")
    var coldGearReq: Double = 0.0,
    @CanBinding(CCSToAC1_Id, "CCSToAC1_FanGearReq")
    @CanSignalBinding("CCSToAC1_FanGearReq")
    var fanGearReq: Double = 0.0,
    @CanBinding(CCSToAC1_Id, "heart")
    @CanSignalBinding("heart")
    var heart: Double = 0.0,
) : CanCopyable<CCSToAC1> {
    override fun copyNew(): CCSToAC1 {
        return this.copy()
    }
}