package io.github.shilic.smartDbc.dbc.dataModel.contract

/** 信号访问器；
 *
 * 用于直接 访问 DBC 对象中的信号值。
 * */
interface SignalAccessor {
    /** 当前物理值 */
    var currentPhyValue: Double
    /** 当前总线值 */
    var currentHexValue: Long
    /** 当前显示值 */
    var currentTextValue: String
    /** 有效性 */
    val validity: Boolean
}