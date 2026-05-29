package io.github.shilic.smartDbc.dbc.dataModel.attributes

import io.github.shilic.smartGrid.core.*

/**
 * 自定义属性 (允许子类继承)
 * */
@GridSheetBind(sheetName = "AttributeDefinition", pattern = "AttributeDefinition", gridSheetType = GridSheetType.Dictionary)
open class DbcAttributeDefinition: IGridRowData {
    // IGridRowData 接口实现
    override val gridKey: String get() = attributeName
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    /** 自定义属性名称 */
    @GridColumnBind(headerText = "自定义属性名称", pattern = "自定义属性名称", valueType = GridValueType.Text, keyword = true)
    var attributeName: String = ""
    /** 自定义属性描述 */
    @GridColumnBind(headerText = "自定义属性描述", pattern = "自定义属性描述", valueType = GridValueType.Text)
    var attributeDescription: String = ""
    /** 自定义属性作用域(目标类型), 例如是标记在哪个类上的 */
    @GridColumnBind(headerText = "自定义属性作用域", pattern = "自定义属性作用域", valueType = GridValueType.Text)
    var attributeScope: String = ""
    /** 自定义属性值类型 */
    @GridColumnBind(headerText = "自定义属性值类型", pattern = "自定义属性值类型", valueType = GridValueType.Enumeration)
    var dbcAttributeValueType: DbcAttributeValueType = DbcAttributeValueType.StringType
    val attributeValueClass : Class<*> get() = dbcAttributeValueType.valueClass

    /** 自定义属性最小值; 仅整形、浮点型、16进制值时有效。 */
    @GridColumnBind(headerText = "自定义属性最小值", pattern = "自定义属性最小值", valueType = GridValueType.Custom)
    var attributeMinValue: String = ""
    /** 自定义属性的最大值; 仅整形、浮点型、16进制值时有效。 */
    var attributeMaxValue: String = ""
    /** 自定义属性的默认值; 所有的数值类型均有效 */
    var attributeDefaultValue: String = ""

    /** 自定义属性的枚举值; 仅枚举型时有效 */
    var attributeEnumValues: MutableSet<String>  = mutableSetOf()
}