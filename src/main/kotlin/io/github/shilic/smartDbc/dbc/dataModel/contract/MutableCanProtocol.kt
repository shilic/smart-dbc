package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartGrid.core.IMutableGridRowData

/** 可修改的 [MutableCanProtocol] */
interface MutableCanProtocol<D, M, S>: CanProtocol, IMutableGridRowData where D: MutableDataBaseCan<M, S>, M: MutableCanMessage<S>, S: MutableCanSignal {
    override var protocolName: String
    override var protocolComment: String
    override var dbcMap: MutableMap<String, D>

    override operator fun get(dbcTag: String): D? = dbcMap[dbcTag]
    fun set(value: D) = dbcMap.put(value.dbcKey, value)
}