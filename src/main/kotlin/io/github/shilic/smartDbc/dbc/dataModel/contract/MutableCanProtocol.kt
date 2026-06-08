package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartGrid.core.IMutableGridRowData
import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableDataBaseCan as MDbc
import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanMessage as MMsg
import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanSignal as MSig
import io.github.shilic.smartDbc.dbc.attributes.contract.MutableDbcAttributeDefinition as MAttr

/** 可修改的 [MutableCanProtocol] */
interface MutableCanProtocol<D, M, S, A>: CanProtocol, IMutableGridRowData where D: MDbc<M, S, A>, M: MMsg<S>, S: MSig, A : MAttr {
    override var protocolName: String
    override var protocolComment: String
    override var dbcMap: MutableMap<String, D>

    override operator fun get(dbcTag: String): D? = dbcMap[dbcTag]
    fun set(value: D) = dbcMap.put(value.dbcKey, value)
}