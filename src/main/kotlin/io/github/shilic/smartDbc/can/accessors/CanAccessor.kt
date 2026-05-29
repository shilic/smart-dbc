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
     * -> 到绑定的默认接受者字段
     * */
    fun writeCanValue(value: Double) {
        super<SignalAccessor>.currentPhyValue = value
        super<KPropertyAccessor>.setPropertyValue(value)
    }
    /**  写入CAN值：
     *
     * -> 到DBC对象
     *
     * -> 到指定接受者字段
     * */
    fun writeCanValue(owner: Any, value: Double){
        super<SignalAccessor>.currentPhyValue = value
        super<KPropertyAccessor>.setPropertyValue(owner, value)
    }
    /**  读取CAN值：
     *
     * -> 优先从从绑定的默认接受者字段
     *
     * -> 其次从DBC对象
     * */
    fun readCanValue() : Double {
        return super<KPropertyAccessor>.getPropertyValue() ?: super<SignalAccessor>.currentPhyValue
    }
    /**  读取CAN值：
     *
     * -> 优先从从指定接受者字段
     *
     * -> 其次从DBC对象
     * */
    fun readCanValue(owner: Any) : Double {
        return super<KPropertyAccessor>.getPropertyValue(owner) ?: super<SignalAccessor>.currentPhyValue
    }
}