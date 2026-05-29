package io.github.shilic.smartDbc.dbc.dataModel.models

import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanProtocol
import io.github.shilic.smartGrid.core.*

@GridSheetBind(sheetName = "CanProtocol_Info", pattern = "CanProtocol_Info", gridSheetType = GridSheetType.Single)
class CanProtocolImp: MutableCanProtocol<CanDbcImp, CanMessageImp, CanSignalImp> {
    // +++++++++++++++++  IGridRowData 接口实现  +++++++++++++++++
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    @GridColumnBind(headerText = "协议名称", pattern = "协议名称", valueType = GridValueType.Text, keyword = true)
    override var protocolName: String = ""
    @GridColumnBind(headerText = "协议描述", pattern = "协议描述", valueType = GridValueType.Text)
    override var protocolComment: String = ""
    @GridColumnBind(headerText = "DbcList", valueType = GridValueType.OtherSheet)
    override var dbcMap: MutableMap<String, CanDbcImp> = mutableMapOf()
}