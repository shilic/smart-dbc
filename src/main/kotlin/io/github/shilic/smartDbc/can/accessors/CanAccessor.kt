package io.github.shilic.smartDbc.can.accessors

import io.github.shilic.smartDbc.dbc.dataModel.contract.SignalAccessor

/** CAN访问器
 *
 * 继承了 [KPropertyAccessor] 和 [SignalAccessor] 兼容了两种访问方式，综合访问一个信号值。
 * */
interface CanAccessor : KPropertyAccessor, SignalAccessor {
    /**  写入CAN值：
     *
     * -> 到DBC对象
     *
     * -> 到指定接受者字段; [newOwner] 参数为空时，使用默认接受者
     * */
    fun writeCanValue(value: Double, newOwner: Any? = null) {
        currentPhyValue = value
        setPropertyValue(value, newOwner)
    }
    /**  读取CAN值：
     *
     * -> 优先从指定接受者字段读取值; [newOwner] 参数为空时，使用默认接受者
     *
     * -> 如果绑定字段值为空, 其次从DBC对象读取信号值
     * */
    fun readCanValue(newOwner: Any? = null) : Double = getPropertyValue(newOwner) ?: currentPhyValue
}