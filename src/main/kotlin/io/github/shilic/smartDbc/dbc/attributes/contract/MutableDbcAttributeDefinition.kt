package io.github.shilic.smartDbc.dbc.attributes.contract

import io.github.shilic.smartDbc.dbc.attributes.enums.*
import io.github.shilic.smartGrid.core.IMutableGridRowData
import io.github.shilic.smartGrid.core.IMutableValueTable

/** 可修改的属性定义 */
interface MutableDbcAttributeDefinition : DbcAttributeDefinition, IMutableValueTable, IMutableGridRowData {
    override var name: String
    override var comment: String
    override var scope: DbcAttributeScopeDefinition
    override var valueType: DbcAttributeValueType
    override var min: String
    override var max: String
    override var defaultValue: String
    override var valueTable: MutableMap<Int, String>
    override var aValue: String
}