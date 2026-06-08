package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.GridColumnBind

/** 常量 +  */
const val PLUS = "+"
/** 常量 - */
const val MINUS = "-"

/** 数据类型，只有 Unsigned、Signed、Float、Double 四种; 默认 Unsigned;
 *
 * [CanDataType.Unsigned] 无符号 , 显示+
 *
 * [CanDataType.Signed] 有符号， 显示 -
 *
 * [CanDataType.Float] 单精度浮点数， 显示 - ; 长度固定为 32bit ; 另外标注 SIG_VALTYPE_ = 1
 *
 * [CanDataType.Double] 双精度浮点数， 显示 - ; 长度固定为 64bit ;  另外标注 SIG_VALTYPE_ = 2
 * */
enum class CanDataType (
    override val intValue: Int,
    override val dbcKey : String,
    override val dbcValue : String
) : IDbcElement, IntEnum<CanDataType> {
    /**  [CanDataType.Unsigned] 无符号, 显示+ */
    @GridColumnBind(headerText = "Unsigned", pattern = "Unsigned|Hex|hex|HEX|无符号|((Hex|hex|HEX)?\\s*[(（]?\\s*(Unsigned|unsigned|UNSIGNED)\\s*[)）]?)")
    Unsigned (0, "Unsigned", PLUS),
    /** [CanDataType.Signed] 有符号， 显示 - */
    @GridColumnBind(headerText = "Singed", pattern = "有符号|(?<!(Un|un|UN))Signed|(?<!(Un|un|UN))signed|(?<!(Un|un|UN))SIGNED")
    Signed (1, "Signed", MINUS),
    /* SIG_VALTYPE_ 3221225472 Double_Signal_85 : 2;
    * SIG_VALTYPE_ 3221225472 Float_Signal_84 : 1;
    * 有符号类型的 Float 和 Double是特殊项，除了要用- 号标识是有符号之外，
    * 还需要上边的语句来标注 ： 1 表示 Float； 2 表示 Double ；
    *  */
    /** [CanDataType.Float] 单精度浮点数， 显示 - ; 长度固定为 32bit ; 另外标注 SIG_VALTYPE_ = 1 */
    @GridColumnBind(headerText = "Float", pattern = "Float|float|FLOAT|单精度浮点数")
    Float (2, "Float", MINUS),
    /** [CanDataType.Double] 双精度浮点数， 显示 - ; 长度固定为 64bit ;  另外标注 SIG_VALTYPE_ = 2 */
    @GridColumnBind(headerText = "Double", pattern = "Double|double|DOUBLE|双精度浮点数")
    Double (3, "Double", MINUS);
    companion object{
        fun createBy(dbcValue: String) = when(dbcValue) {
            PLUS -> Unsigned
            MINUS -> Signed
            else -> error("符号 '+' 表示无符号数, 符号 '-' 表示有符号数, 其余字符均为非法字符，无法转换为数据类型")
        }
    }
}
