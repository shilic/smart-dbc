package io.github.shilic.smartDbc.dbc.dataModel.models

import io.github.shilic.smartDbc.dbc.attributes.models.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.*

import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableDataBaseCan as MDbc

/**  单个 dbc 对象 */
@GridSheetBind(sheetName = "DbcList", pattern = "DbcList", gridSheetType = GridSheetType.Dictionary)
open class DataBaseCanImp: MDbc<CanMessageImp, CanSignalImp, DbcAttributeDefinitionImp> {
    // ------------------------- 基本信息 ---------------------
    @GridColumnBind(headerText = "DBC英文名", pattern = "DBC英文名", valueType = GridValueType.Text, keyword = true)
    override var dbcTag: String = ""
    @GridColumnBind(headerText = "DBC版本", pattern = "DBC版本", valueType = GridValueType.Text)
    override var version: String = ""
    @GridColumnBind(headerText = "DBC描述", pattern = "DBC描述", valueType = GridValueType.Text)
    override var dbcComment: String = ""
    @GridColumnBind(headerText = "节点列表", pattern = "节点列表", valueType = GridValueType.Strings)
    override var nodeSet: MutableSet<String> = hashSetOf()
    @GridColumnBind(headerText = "波特率", pattern = "波特率", valueType = GridValueType.NumberType)
    override var baudRate: Int = 500

    override var attributeMap: MutableMap<String, DbcAttributeDefinitionImp> = mutableMapOf()
    // ------------------------ 子数据 ------------------------
    @GridColumnBind(headerText = "CAN1", valueType = GridValueType.SpecificSheet)
    override var msgMap: MutableMap<String, CanMessageImp> = mutableMapOf()

    // +++++++++++++++ 实现 IGridSpecificSheet 接口 ++++++++++++++
    @GridColumnBind(headerText = "DBC页面名称", pattern = "DBC页面名称", valueType = GridValueType.Text)
    override var specificSheetName: String = ""

    // +++++++++++++ IGridRowData 接口实现 +++++++++++++++
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null
    @Transient
    override var subDataMap: MutableMap<Int, Any> = mutableMapOf()
    override fun toString(): String = buildString {
        appendLine(baseInfo)
        for (msg in msgMap.values) {
            appendLine("\t${msg.baseInfo}")
            for (signal in msg.signalMap.values){
                appendLine("\t\t${signal.baseInfo}")
            }
        }
    }
}
