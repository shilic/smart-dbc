package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.IDbcElement
import io.github.shilic.smartGrid.core.GridColumnBind

/** 报文发送模式
 *
 *  [GenMsgSendType.Cycle] : 周期型, 序号 0 ;
 *
 *  [GenMsgSendType.Event] : 事件型, 序号 1 ;
 *
 *  [GenMsgSendType.IfActive] : 激活型, 序号 2 ;
 *
 *  [GenMsgSendType.CE] : 持续型, 序号 3 ;
 *
 *  [GenMsgSendType.CA] : 持续型(激活型) , 序号 4
 *
 *  */
enum class GenMsgSendType (
    override val intValue: Int ,
    override val dbcKey : String,
    override val dbcValue : String
) : IDbcElement, IntEnum<GenMsgSendType> {
    /** [GenMsgSendType.Cycle] : 周期型, 序号 0 */
    @GridColumnBind(headerText = "Cycle", pattern = "Cyclic|CYCLIC|cyclic|Cycle|cycle|CYCLE")
    Cycle (0, "Cycle", "0"),
    /** [GenMsgSendType.Event] : 事件型, 序号 1 */
    @GridColumnBind(headerText = "Event", pattern = "Event|event|EVENT")
    Event (1, "Event", "1"),
    /** [GenMsgSendType.IfActive] : 激活型, 序号 2 */
    @GridColumnBind(headerText = "IfActive", pattern = "IfActive|((If|if|IF)\\s*(Active|active|ACTIVE))")
    IfActive (2, "IfActive", "2"),
    /** [GenMsgSendType.CE] : 持续型, 序号 3 */
    @GridColumnBind(headerText = "CE", pattern = "CE")
    CE (3, "CE", "3"),
    /** [GenMsgSendType.CA] : 持续型(激活型) , 序号 4*/
    @GridColumnBind(headerText = "CA", pattern = "CA")
    CA (4, "CA", "4")
}
