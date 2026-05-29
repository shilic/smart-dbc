package io.github.shilic.smartDbc.can.contract

/** 自定义拷贝接口，用于拷贝数据模型 */
interface CanCopyable<T> {
    /** 返回拷贝之后的数据模型 */
    fun copyNew(): T
}
