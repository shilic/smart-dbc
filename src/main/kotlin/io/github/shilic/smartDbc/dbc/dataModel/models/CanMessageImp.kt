package io.github.shilic.smartDbc.dbc.dataModel.models

import io.github.shilic.smartDbc.dbc.attributes.models.DbcAttributeData
import io.github.shilic.smartDbc.dbc.dataModel.Vector__XXX
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.*
import kotlin.collections.mutableMapOf
import kotlin.text.appendLine
import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanMessage as MMsg

/** 用于描述消息 Message */
@GridSheetBind(gridSheetType = GridSheetType.Dictionary)
open class CanMessageImp: MMsg<CanSignalImp> {
    // ----------------------- 基本信息 -----------------------
    @GridColumnBind(headerText = "报文名称", pattern = "报文名称|MsgName|((Msg|msg|MSG|Message|MESSAGE|message)\\s*(Name|name|NAME))", valueType = GridValueType.Text, uiIgnore = true, keyword = true)
    override var msgName: String = ""
    // @GridColumnBind(headerText = "MsgType", pattern = "报文类型|((Msg|msg|MSG)\\s*(Type|type|TYPE))", valueType = GridValueType.Custom, customAdapterName = "")
    override var msgIdType: CanExternFlag = CanExternFlag.Extended
        get() = CanMessage.calculateCanMsgIdTypeById(msgId)
    @GridColumnBind(headerText = "报文标识符", pattern = "报文标识符|MsgID|((Msg|msg|MSG|Message|MESSAGE|message)\\s*(ID|id|Id|identification|Identification))", valueType = GridValueType.HexNumber)
    override var msgId: Int = 0
    @GridColumnBind(headerText = "报文发送模式",
        pattern = "报文发送模式|MsgSendType|((Msg|msg|MSG|Message|MESSAGE|message)\\s*(Send|send|SEND)\\s*(Type|type|TYPE))",
        valueType = GridValueType.Enumeration)
    override var genMsgSendType: GenMsgSendType = GenMsgSendType.Cycle
    @GridColumnBind(headerText = "报文周期时间",
        pattern = "报文周期时间|MsgCycleTime|((Msg|msg|MSG|Message|MESSAGE|message)\\s*(Cyclic|CYCLIC|cyclic|Cycle|cycle|CYCLE)\\s*(Time|time|TIME))",
        valueType = GridValueType.NumberType)
    override var msgCycleTime: Int = 200
    @GridColumnBind(headerText = "报文长度", pattern = "报文长度|MsgLength|((Msg|msg|MSG|Message|MESSAGE|message)\\s*(Length|length|LENGTH))", valueType = GridValueType.NumberType)
    override var msgLength: Int = 8
    @GridColumnBind(headerText = "Remark", pattern = "Remark|备注", valueType = GridValueType.Text)
    override var msgComment: String = ""

    // -------------------------- 节点信息 ------------------------
    //@GridColumnBind(headerText = "nodeName", pattern = "nodeName", valueType = GridValueType.Custom, customAdapterName = "")
    override var nodeName: String = Vector__XXX
    //@GridColumnBind(headerText = "msgReceiveNodeList", pattern = "msgReceiveNodeList", valueType = GridValueType.Custom, customAdapterName = "")
    override var msgReceiveNodeSet: MutableSet<String> = hashSetOf()
    @GridColumnBind(headerText = "SignalName", pattern = "信号名称|((Signal|signal|SIGNAL)\\s*(Name|name|NAME)\\s*)(?!([(（]?\\s*(Chinese|chinese|CHINESE)\\s*[）)]?))", valueType = GridValueType.SubSignal)
    override var signalMap: MutableMap<String, CanSignalImp> = mutableMapOf()
    override var attributeValueMap: MutableMap<String, DbcAttributeData> = mutableMapOf()

    // ++++++++++++++++++++++++ IGridRowData 接口实现 +++++++++++++++++++++++
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null
    @Transient
    override var subDataMap: MutableMap<Int, Any> = mutableMapOf()
    override fun toString(): String = buildString {
        appendLine(baseInfo)
        for (signal in signalMap.values){
            appendLine("\t${signal.baseInfo}")
        }
    }
}
