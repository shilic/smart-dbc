package io.github.shilic.smartDbc.dbc.attributes.contract

import io.github.shilic.smartDbc.dbc.attributes.enums.*
import io.github.shilic.smartDbc.dbc.dataModel.BA_
import io.github.shilic.smartDbc.dbc.dataModel.contract.IDbcElement

/** 自定义只读的属性值 */
interface DbcAttributeValue : IDbcElement {
    override val dbcKey: String get() = name
    /** 只读的自定义属性类型定义 */
    val definition: DbcAttributeDefinition
    /** 自定义属性的名称; 固定为属性定义的名称 */
    val name : String get() = definition.name
    /** 自定义属性作用域数据
     *
     * 作用域数据，用于生成DBC文件的属性作用域数据;
     *
     * [DbcAttributeScopeData.Net] 网络类型, 在DBC文件中的编码类似 BA_ "DBName" "Example";, 表示整个DBC文件的自定义属性;
     *
     * [DbcAttributeScopeData.Message] 报文类型, 在DBC文件中的编码类似 BA_ "GenMsgCycleTime" BO_ 2560107544 500;
     *
     * [DbcAttributeScopeData.Signal] 信号类型, 在DBC文件中的编码类似 BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_FactoryID 0;
     *
     * [DbcAttributeScopeData.Node] 节点类型, 在DBC文件中的编码类似  BA_ "New_AttrDef_14" BU_ CCS 3.14159;
     *
     * */
    val scopeData: DbcAttributeScopeData
    /** 统一使用字符串来保存自定义属性的值, 统一为在DBC文件当中的显示值;
     *
     * -> 整形、浮点型 = 例如 1234, 3.14,
     *
     * -> String 时为字符串 = 例如 "hello world"
     *
     * -> 枚举时，为枚举值的序号 = 例如 0 , 1
     *
     * -> 十六进制时，统一为整形的十进制值，非常智障
     * */
    val value: String


    /** 返回DBC编码，例如：
     *
     * BA_ "DBName" "Example";
     *
     * BA_ "NmMessage" BO_ 2560107544 0;
     *
     * BA_ "DiagState" BO_ 2560107544 0;
     *
     * BA_ "GwUsedMsg" BO_ 2560107544 0;
     *
     * BA_ "GenMsgCycleTime" BO_ 2560107544 500;
     *
     * BA_ "GenMsgSendType" BO_ 2560107544 1;
     * */
    override val dbcValue: String get() = "$BA_ \"${name}\" ${scopeData.dbcValue} $value;"

    /**
     * 获取自定义属性值，并转换为整形
     *
     * @return 转换后的整形值
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为整形
     */
    fun getIntValue(): Int {
        require(definition.valueType == DbcAttributeValueType.IntegerType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为整形数值" }
        return runCatching { value.toInt() }.getOrElse { exception -> error("将字符串值 '${value}' 转换为整形时出错: ${exception.message}") }
    }
    /**
     * 获取自定义属性值，并转换为浮点型
     *
     * @return 转换后的浮点型值
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为浮点型
     */
    fun getFloatValue(): Float {
        require(definition.valueType == DbcAttributeValueType.FloatType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为浮点数值" }
        return runCatching { value.toFloat() }.getOrElse { exception -> error("将字符串值 '${value}' 转换为浮点数值时出错: ${exception.message}") }
    }
    /**
     * 获取自定义属性值，并转换为字符串
     *
     * @return 转换后的字符串值
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为字符串
     */
    fun getStringValue(): String {
        require(definition.valueType == DbcAttributeValueType.StringType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为字符串" }
        return value
    }
    /**
     * 获取自定义属性值，并转换为枚举值
     *
     * @return 枚举值
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为枚举值
     */
    fun getEnumText(): String {
        require(definition.valueType == DbcAttributeValueType.Enumeration) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为枚举值" }
        return runCatching { definition.valueTable[value.toInt()] ?: error("枚举值索引 '${value}' 不存在") }
            .getOrElse { exception -> error("将字符串值 '${value}' 转换为枚举字符串时出错: ${exception.message}") }
    }
    /**
     * 获取自定义属性值，并转换为枚举索引
     *
     * @return 枚举索引
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为枚举索引
     */
    fun getEnumIndex(): Int {
        require(definition.valueType == DbcAttributeValueType.Enumeration) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为枚举值" }
        return runCatching { value.toInt() }.getOrElse { exception -> error("将字符串值 '${value}' 转换为枚举索引时出错: ${exception.message}") }
    }
    /**
     * 获取自定义属性值，十六进制时，统一为整形的十进制值
     *
     * @return 十六进制值
     * @throws IllegalArgumentException 如果自定义属性的值无法转换为整形值
     */
    fun getHexValue(): Int {
        require(definition.valueType == DbcAttributeValueType.HexType) { "自定义属性 '${definition.name}' 的类型是 '${definition.valueType}' , 无法转换为16进制整形数值" }
        return runCatching { value.toInt() }.getOrElse { exception -> error("将字符串值 '${value}' 转换为整形值时出错: ${exception.message}") }
    }
}