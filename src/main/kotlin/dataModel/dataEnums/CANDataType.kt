package dataModel.dataEnums

import dataModel.services.IDbcElement
import io.github.shilic.smartGrid.core.GridColumnBind

/** 数据类型，
 *
 * [Unsigned] 无符号 , 显示+
 *
 * [Signed] 有符号， 显示 -
 *
 * [Float] 单精度浮点数， 显示 - ; 长度固定为 32bit ; 另外标注 SIG_VALTYPE_ = 1
 *
 * [Double] 双精度浮点数， 显示 - ; 长度固定为 64bit ;  另外标注 SIG_VALTYPE_ = 2
 * */
enum class CANDataType (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override  val dbcValue : String
) : IDbcElement {
    /**  [Unsigned] 无符号, 显示+ */
    @GridColumnBind(headerText = "Unsigned", pattern = "Unsigned|Hex|hex|HEX|无符号|((Hex|hex|HEX)?\\s*[(（]?\\s*(Unsigned|unsigned|UNSIGNED)\\s*[)）]?)")
    Unsigned ("Unsigned", "+"),
    /** [Signed] 有符号， 显示 - */
    @GridColumnBind(headerText = "Singed", pattern = "有符号|(?<!(Un|un|UN))Signed|(?<!(Un|un|UN))signed|(?<!(Un|un|UN))SIGNED")
    Signed ("Signed", "-"),
    /* SIG_VALTYPE_ 3221225472 Double_Signal_85 : 2;
    * SIG_VALTYPE_ 3221225472 Float_Signal_84 : 1;
    * 有符号类型的 Float 和 Double是特殊项，除了要用- 号标识是有符号之外，
    * 还需要上边的语句来标注 ： 1 表示 Float； 2 表示 Double ；
    *  */
    /** [Float] 单精度浮点数， 显示 - ; 长度固定为 32bit ; 另外标注 SIG_VALTYPE_ = 1 */
    @GridColumnBind(headerText = "Float", pattern = "Float|float|FLOAT|单精度浮点数")
    Float ("Float", "-"),
    /** [Double] 双精度浮点数， 显示 - ; 长度固定为 64bit ;  另外标注 SIG_VALTYPE_ = 2 */
    @GridColumnBind(headerText = "Double", pattern = "Double|double|DOUBLE|双精度浮点数")
    Double ("Double", "-"),
    /** 未定义 */
    CANDataTypeNotDefine ("CANDataTypeNotDefine", "+");
}
