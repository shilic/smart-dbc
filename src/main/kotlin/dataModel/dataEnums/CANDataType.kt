package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 数据类型，
 *
 * Unsigned 无符号 , 显示+
 *
 * Signed 有符号， 显示 -
 *
 * Float 浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 1
 *
 * Double 双精度浮点数， 显示 - ; 另外标注 SIG_VALTYPE_ = 2
 * */
enum class CANDataType (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override  val dbcValue : String
) : IDbcElement {
    /** 无符号, 显示+ */
    Unsigned ("Unsigned", "+"),
    /** 有符号， 显示 - */
    Signed ("Signed", "-"),
    /* SIG_VALTYPE_ 3221225472 Double_Signal_85 : 2;
    * SIG_VALTYPE_ 3221225472 Float_Signal_84 : 1;
    * 有符号类型的 Float 和 Double是特殊项，除了要用- 号标识是有符号之外，
    * 还需要上边的语句来标注 ： 1 表示 Float； 2 表示 Double ；
    *  */
    /** 浮点数， 显示 -  */
    Float ("Float", "-"),
    /** 双精度浮点数， 显示 -  */
    Double ("Double", "-"),
    /** 未定义 */
    CANDataTypeNotDefine ("CANDataTypeNotDefine", "+");
}
