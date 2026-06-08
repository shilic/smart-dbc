package io.github.shilic.smartDbc.dbc.attributes.models

import io.github.shilic.smartDbc.dbc.attributes.contract.*
import io.github.shilic.smartDbc.dbc.attributes.enums.*
import io.github.shilic.smartDbc.valueConverter.findFirstIndexByValue


/** 自定义属性值  */
data class DbcAttributeData (
    override val definition: DbcAttributeDefinitionImp = DbcAttributeDefinitionImp(),
    override val scopeData: DbcAttributeScopeData
) : DbcAttributeValue {
    /** 后背字段 */
    private var mValue: String = ""
    override var value: String
        get() = mValue
        // ----------------- 类型不安全的设置器，需要检查类型 -----------------
        set(value) {
            mValue = value.takeIf { it.isNotBlank() }?.takeIf { definition.isValid(it) }
                ?: error("设置自定义属性 '${definition.name}' 的值失败,  '${value}' 不是有效的值")
        }

    // ----------------- 提供类型更安全的设置器，因为是类型安全的，所以没有检查类型 -----------------
    fun setIntValue(value: Int) {
        require(definition.valueType == DbcAttributeValueType.IntegerType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为整形数值" }
        require(definition.isInRange(value.toString())) { "自定义属性 '${definition.name}' 的值 '${value}' 超出范围: '${definition.range}' " }
        this.mValue = value.toString()
    }
    fun setFloatValue(value: Float) {
        require(definition.valueType == DbcAttributeValueType.FloatType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为浮点数值" }
        require(definition.isInRange(value.toString())) { "自定义属性 '${definition.name}' 的值 '${value}' 超出范围: '${definition.range}' " }
        this.mValue = value.toString()
    }
    fun setStringValue(value: String) {
        require(definition.valueType == DbcAttributeValueType.StringType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为字符串" }
        this.mValue = value
    }
    fun setEnumText(value: String) {
        require(definition.valueType == DbcAttributeValueType.Enumeration) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为枚举值" }
        require(definition.valueTable.values.contains(value)) { "自定义属性 '${definition.name}' 的枚举值 '${value}' 不在枚举值列表中: '${definition.valueTable}' " }
        this.mValue = definition.valueTable.findFirstIndexByValue(value)!!.toString()
    }
    fun setEnumIndex(value: Int) {
        require(definition.valueType == DbcAttributeValueType.Enumeration) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为枚举值" }
        require(definition.valueTable.containsKey(value)) { "自定义属性 '${definition.name}' 的枚举序号 '${value}' 不在枚举值列表中: '${definition.valueTable}' " }
        this.mValue = value.toString()
    }
    fun setHexValue(value: Int) {
        require(definition.valueType == DbcAttributeValueType.HexType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为16进制数值" }
        require(definition.isInRange(value.toString())) { "自定义属性 '${definition.name}' 的值 '${value}' 超出范围: '${definition.range}' " }
        this.mValue = value.toString()
    }


}