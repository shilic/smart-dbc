package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartGrid.core.*
import  io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanSignal as MSig

/** 提供可变的 CanMessage; */
interface MutableCanMessage<S>:  CanMessage, IMutableGridRowData, MutableSubDataOwner where S: MSig {
    // ----------------------- 基本信息 -----------------------
    override var msgName: String
    override var msgIdType: CanExternFlag
    override var msgId: Int
    override var genMsgSendType: GenMsgSendType
    override var msgCycleTime: Int
    override var msgLength: Int
    override var msgComment: String

    // ----------------------- 节点信息 ------------------------
    override var nodeName: String
    override var msgReceiveNodeSet: MutableSet<String>
    override var signalMap: MutableMap<String, S>

    // ======================= 索引器 ==========================
    override operator fun get(signalName: String): S? = signalMap[signalName]
    fun set(canSignal : S) = signalMap.put(canSignal.dbcKey, canSignal)
}