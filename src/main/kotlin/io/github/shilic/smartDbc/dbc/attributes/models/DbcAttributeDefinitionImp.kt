package io.github.shilic.smartDbc.dbc.attributes.models

import io.github.shilic.smartDbc.dbc.attributes.contract.MutableDbcAttributeDefinition
import io.github.shilic.smartDbc.dbc.attributes.enums.DbcAttributeScopeDefinition
import io.github.shilic.smartDbc.dbc.attributes.enums.DbcAttributeValueType
import io.github.shilic.smartGrid.core.*

/**  自定义属性定义实现类 */
@GridSheetBind(sheetName = "AttributeDefinition", pattern = "AttributeDefinition", gridSheetType = GridSheetType.Dictionary)
open class DbcAttributeDefinitionImp: MutableDbcAttributeDefinition {
    // ++++++++++++++++++++++++++ IGridRowData 接口实现  +++++++++++++++++++++++++++++++
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    @GridColumnBind(headerText = "自定义属性名称", pattern = "自定义属性名称", valueType = GridValueType.Text, keyword = true)
    override var name: String = ""
    @GridColumnBind(headerText = "自定义属性描述", pattern = "自定义属性描述", valueType = GridValueType.Text)
    override var comment: String = ""
    @GridColumnBind(headerText = "自定义属性作用域", pattern = "自定义属性作用域", valueType = GridValueType.Text)
    override var scope: DbcAttributeScopeDefinition = DbcAttributeScopeDefinition.Net
    @GridColumnBind(headerText = "自定义属性值类型", pattern = "自定义属性值类型", valueType = GridValueType.Enumeration)
    override var valueType: DbcAttributeValueType = DbcAttributeValueType.StringType
    @GridColumnBind(headerText = "自定义属性最小值", pattern = "自定义属性最小值", valueType = GridValueType.Custom)
    override var min: String = "0"
        set(value) { value.takeIf { it.isNotBlank() }?.takeIf { isNumberValid(it) }?.let { field = it } }

    override var max: String = "0"
        set(value) { value.takeIf { it.isNotBlank() }?.takeIf { isNumberValid(it) }?.let { field = it } }

    override var defaultValue: String = ""
        set(value) { value.takeIf { it.isNotBlank() }?.let { field = it } }

    override var valueTable: MutableMap<Int, String> = mutableMapOf()
    override var aValue: String = ""
    override fun toString(): String = "DbcAttributeDefinition(name=$name, comment=$comment, scope=$scope, valueType=$valueType, defaultValue=$defaultValue, range=$range)"
}