package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartGrid.core.IGridRowData

/** 不可修改的 CanProtocol */
interface CanProtocol : IGridRowData  {
    // +++++++++++++++++  IGridRowData 接口实现  +++++++++++++++++
    override val gridKey: String get() = protocolName
    /** 协议名称 */
    val protocolName: String
    /** 协议描述 */
    val protocolComment: String
    /** DBC对象 */
    val dbcMap: Map<String, CanDbc>

    operator fun get(dbcTag: String): CanDbc? = dbcMap[dbcTag]
}