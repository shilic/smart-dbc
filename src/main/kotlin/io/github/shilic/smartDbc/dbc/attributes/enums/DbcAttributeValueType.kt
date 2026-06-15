package io.github.shilic.smartDbc.dbc.attributes.enums

import io.github.shilic.smartDbc.dbc.dataModel.contract.IDbcElement
import kotlin.reflect.KClass

/** 常量: INT */
const val INT = "INT"
/** 常量: FLOAT */
const val FLOAT = "FLOAT"
/** 常量: STRING */
const val STRING = "STRING"
/** 常量: ENUM */
const val ENUM = "ENUM"
/** 常量: HEX */
const val HEX = "HEX"

/**  自定义属性值的类型
 *
 *  [DbcAttributeValueType.IntegerType] 整数, 在DBC文件中是 [INT] , 范围是两个十进制整形数值
 *
 *  [DbcAttributeValueType.FloatType] 浮点数, 在DBC文件中是 [FLOAT] , 范围是两个十进制浮点型数值
 *
 *  [DbcAttributeValueType.StringType] 文本, 在DBC文件中是 [STRING] , 在DBC文件中无范围
 *
 *  [DbcAttributeValueType.Enumeration] 枚举, 在DBC文件中是 [ENUM] ; 范围以英文双引号包裹、以逗号分割的序列表示枚举项, 项不重复; 枚举项的排序(从0开始记)表示枚举的实际值 。默认值是用字符串文本在保存，而枚举实际值却是枚举索引，我受不了了。
 *
 *  [DbcAttributeValueType.HexType] 十六进制数值, 在DBC文件中是 HEX ;
 *  注意, 虽然说是16进制数值类型, 但是在DBC文件中, 该数值的最大值、最小值、默认值以及实际值均是以十进制的方式在保存;
 *  可以说非常智障了; 该数据类型和十进制整形基本没有任何区别;
 *
 * */
enum class DbcAttributeValueType (
    /** 值类型对应的 KClass */
    val valueClass: KClass<*>,
    override val dbcKey : String,
    /**  自定义属性值的类型
     *
     *  [DbcAttributeValueType.IntegerType] 整数, 在DBC文件中是 [INT] , 范围是两个十进制整形数值
     *
     *  [DbcAttributeValueType.FloatType] 浮点数, 在DBC文件中是 [FLOAT] , 范围是两个十进制浮点型数值
     *
     *  [DbcAttributeValueType.StringType] 文本, 在DBC文件中是 [STRING] , 在DBC文件中无范围
     *
     *  [DbcAttributeValueType.Enumeration] 枚举, 在DBC文件中是 [ENUM] ; 范围以英文双引号包裹、以逗号分割的序列表示枚举项, 项不重复; 枚举项的排序(从0开始记)表示枚举的实际值 。默认值是用字符串文本在保存，而枚举实际值却是枚举索引，我受不了了。
     *
     *  [DbcAttributeValueType.HexType] 十六进制数值, 在DBC文件中是 HEX ;
     *  注意, 虽然说是16进制数值类型, 但是在DBC文件中, 该数值的最大值、最小值、默认值以及实际值均是以十进制的方式在保存;
     *  可以说非常智障了; 该数据类型和十进制整形基本没有任何区别;
     *
     * */
    override val dbcValue : String
): IDbcElement {
    /** [DbcAttributeValueType.IntegerType] 整数, 在DBC文件中是 [INT] , 范围是两个十进制整形数值*/
    IntegerType(Int::class, INT, INT),
    /** [DbcAttributeValueType.FloatType] 浮点数型, 在DBC文件中是 [FLOAT] , 范围是两个十进制浮点型数值 */
    FloatType(Float::class, FLOAT, FLOAT),
    /** [DbcAttributeValueType.StringType] 文本类型型, 在DBC文件中是 [STRING] , 在DBC文件中无范围 */
    StringType(String::class,STRING, STRING),
    /** [DbcAttributeValueType.Enumeration] 枚举, 在DBC文件中是 [ENUM] ;
     *
     * 范围以英文双引号包裹、以逗号分割的序列表示枚举项, 项不重复;
     *
     * 枚举项的排序(从0开始记)表示枚举的实际值 。
     *
     * 默认值是用字符串文本在保存，而枚举实际值却是枚举索引，我受不了了。*/
    Enumeration(String::class, ENUM, ENUM),
    /** [DbcAttributeValueType.HexType] 十六进制数值, 在DBC文件中是 HEX ;
     *
     * 注意, 虽然说是16进制数值类型, 但是在DBC文件中, 该数值的最大值、最小值和默认值均是以十进制的方式在保存; 可以说非常智障了;
     *
     * 该数据类型和十进制整形基本没有任何区别;*/
    HexType(Int::class, HEX, HEX);

    companion object {
        /** 根据字符串创建属性值类型
         *
         *  INT -> IntegerType
         *
         *  FLOAT -> FloatType
         *
         *  STRING -> TextType
         *
         *  ENUM -> Enumeration
         *
         *  HEX -> HexType
         * */
        fun createBy(value: String) :DbcAttributeValueType = when(value.trim()) {
            INT -> IntegerType
            FLOAT -> FloatType
            STRING -> StringType
            ENUM -> Enumeration
            HEX -> HexType
            else -> throw Exception("根据字符串创建 '${DbcAttributeValueType::class.simpleName}'枚举失败， 输入值: $value")
        }
    }
}