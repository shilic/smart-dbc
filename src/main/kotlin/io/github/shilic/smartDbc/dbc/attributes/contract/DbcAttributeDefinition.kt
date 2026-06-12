package io.github.shilic.smartDbc.dbc.attributes.contract

import io.github.shilic.smartDbc.dbc.attributes.enums.*
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.*
import kotlin.reflect.KClass

/** 只读的自定义属性值
 *
 * 例如
 *
 * BA_DEF_ BO_  "New_AttrDef_12_Double" FLOAT 0 0;
 *
 * BA_DEF_ SG_  "GenSigStartValue" INT 0 65535;
 *
 * BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
 *
 * BA_DEF_ BU_  "New_AttrDef_14" FLOAT 0 0;
 *
 * BA_DEF_ BU_  "NmStationAddress" HEX 0 15;
 *
 * BA_DEF_ EV_  "New_AttrDef_17" INT 0 0;
 *
 * BA_DEF_  "DBName" STRING ;
 *
 * BA_DEF_  "BusType" STRING ;
 * */
interface DbcAttributeDefinition : IDbcElement, IValueTable, IGridRowData {
    override val gridKey: String get() = name
    override val dbcKey : String get() = name
    /** DBC编码, 形如
     *
     * BA_DEF_ BO_  "New_AttrDef_12_Double" FLOAT 0 0;
     *
     * BA_DEF_ SG_  "GenSigStartValue" INT 0 65535;
     *
     * BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
     *
     * BA_DEF_ BU_  "New_AttrDef_14" FLOAT 0 0;
     *
     * BA_DEF_ EV_  "New_AttrDef_17" INT 0 0;
     *
     * BA_DEF_  "DBName" STRING ;
     *
     * BA_DEF_  "BusType" STRING ;
     * */
    override val dbcValue : String get() = "$BA_DEF_ ${scope.dbcValue}  \"${name}\" ${valueType.dbcValue} ${range};"

    /** 自定义属性名称 */
    val name: String
    /** 自定义属性描述 */
    val comment: String
    /** 自定义属性的作用域类型
     *
     * [DbcAttributeScopeDefinition.Net] 网络类型, 在DBC文件中的编码为 空字符串 "" , 表示整个DBC文件的自定义属性;
     *
     * [DbcAttributeScopeDefinition.Message] 报文类型, 在DBC文件中的编码为 BO_
     *
     * [DbcAttributeScopeDefinition.Signal] 信号类型, 在DBC文件中的编码为 SG_
     *
     * [DbcAttributeScopeDefinition.Node] 节点类型, 在DBC文件中的编码为 BU_
     * */
    val scope: DbcAttributeScopeDefinition
    /**  自定义属性值的类型
     *
     *  [DbcAttributeValueType.IntegerType] 整数, 在DBC文件中是 INT , 范围是两个十进制整形数值
     *
     *  [DbcAttributeValueType.FloatType] 浮点数, 在DBC文件中是 FLOAT , 范围是两个十进制浮点型数值
     *
     *  [DbcAttributeValueType.StringType] 文本, 在DBC文件中是 STRING , 在DBC文件中无范围
     *
     *  [DbcAttributeValueType.Enumeration] 枚举在, DBC文件中是 ENUM ; 范围以英文双引号包裹、以逗号分割的序列表示枚举项, 项不重复; 枚举项的排序(从0开始记)表示枚举的实际值
     *
     *  [DbcAttributeValueType.HexType] 十六进制数值, 在DBC文件中是 HEX ; 注意, 虽然说是16进制数值类型, 但是在DBC文件中, 该数值的最大值、最小值和默认值均是以十进制的方式在保存; 可以说非常智障了;该数据类型和十进制整形基本没有任何区别;
     *
     * */
    val valueType: DbcAttributeValueType
    val kClass : KClass<*> get() = valueType.valueClass
    /** 自定义属性最小值; 仅整形、浮点型、16进制值时有效。且均为10进制数 */
    val min: String
    /** 自定义属性的最大值; 仅整形、浮点型、16进制值时有效。且均为10进制数 */
    val max: String
    /** 自定义属性的默认值; 所有的数值类型均有效。
     *
     * 整形、浮点型、16进制值时 -> 十进制的数值
     *
     * 文本类型 -> 字符串，或空字符串
     *
     * 枚举类型 -> 枚举项的文本值, 或空字符串; 注意!! 并不是保存的枚举项序号，而是枚举项的文本值; 而在具体的自定义属性值时，才保存枚举项序号，妈的搞扯，为什么不统一定义;
     *
     * */
    val defaultValue: String
    /** 自定义属性的枚举值, 仅枚举型时有效;
     *
     * 键表示枚举的序号, 从0开始记; 值表示枚举的显示值, 不重复; */
    override val valueTable: Map<Int, String>



    /**自定义属性的范围
     *
     * 整形、浮点型、十六进制数值时, 返回形如 0 255 的字符串，使用空格左右的两个字符表示范围;
     *
     * 枚举型时, 返回形如 "枚举项1","枚举项2","枚举项3" 的字符串，使用双引号包裹, 逗号分隔;
     * */
    val range: String get() = when(valueType) {
        DbcAttributeValueType.IntegerType, DbcAttributeValueType.FloatType, DbcAttributeValueType.HexType -> "$min $max"
        DbcAttributeValueType.StringType -> " "
        DbcAttributeValueType.Enumeration -> valueTable.values.joinToString(",") { it -> "\"$it\"" }
    }

    /**
     * 检查给定的值是否是有效的;
     *
     * [DbcAttributeValueType.IntegerType] -> 检查只是否是整形值
     *
     * [DbcAttributeValueType.FloatType] -> 检查是否是浮点值
     *
     * [DbcAttributeValueType.StringType] -> true
     *
     * [DbcAttributeValueType.Enumeration] -> 检查是否是枚举的索引序号
     *
     * [DbcAttributeValueType.HexType] -> 检查是否是整形值(并非16进制字符串)
     *
     * @param value 给定的值
     * @return 是否有效
     */
    fun isValid(value: String): Boolean = when(valueType) {
        DbcAttributeValueType.IntegerType, DbcAttributeValueType.FloatType, DbcAttributeValueType.HexType  -> isNumberValid(value)
        DbcAttributeValueType.StringType -> true
        DbcAttributeValueType.Enumeration -> valueTable.containsKey(value.toInt())
    }
    /**
     * 检查给定的值是否是有效的数值类型;
     *
     * [DbcAttributeValueType.IntegerType] -> 检查只是否是整形值
     *
     * [DbcAttributeValueType.FloatType] -> 检查是否是浮点值
     *
     * [DbcAttributeValueType.HexType] -> 检查是否是整形值(并非16进制字符串)
     *
     * @param value 给定的值
     * @return 是否有效
     */
    fun isNumberValid(value: String): Boolean = when(valueType) {
        DbcAttributeValueType.IntegerType -> value.toIntOrNull() != null
        DbcAttributeValueType.FloatType -> value.toFloatOrNull() != null
        DbcAttributeValueType.HexType -> value.toIntOrNull() != null
        else -> false
    }
    fun isInRange(value: String): Boolean {
        return when(valueType) {
            DbcAttributeValueType.IntegerType -> value.toInt() in min.toInt()..max.toInt()
            DbcAttributeValueType.FloatType -> value.toFloat() in min.toFloat()..max.toFloat()
            DbcAttributeValueType.StringType -> true
            DbcAttributeValueType.Enumeration -> valueTable.containsKey(value.toInt())
            DbcAttributeValueType.HexType -> value.toInt() in min.toInt()..max.toInt()
        }
    }



}