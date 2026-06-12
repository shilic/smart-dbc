package io.github.shilic.smartDbc.dbc.dataModel.contract

import io.github.shilic.smartDbc.dbc.attributes.models.DbcAttributeData
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartGrid.core.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/** 提供可变的 CanSignal ;
 *
 * 基础数据全部由 val 变成 var (kotlin函数式编程风格的API)
 * */
interface MutableCanSignal: IMutableValueTable, IMutableGridRowData, CanSignal {
    // -------------------- 基本信息 ---------------------
    override var signalName: String
    override var signalComment: String

    // -------------------- 信号排列 ----------------------
    override var groupType: MatrixGroupType
    override var byteOrder: CanByteOrder
    override var genSigSendType: GenSigSendType
    override var startBit: Int
    override var bitLength: Int
    override var dataType: CanDataType
    override var factor: Double
    override var offset: Double

    // --------------------- 物理值 -----------------------
    override var signalMinValuePhys: Double
    override var signalMaxValuePhys: Double
    override var initialValuePhys: Double

    // -------------- 原始值(总线值/未处理值) ----------------
    override var signalMinValueHex: Long
    override var signalMaxValueHex: Long
    override var initialValueHex: Long
    override var invalidValueHex: Long

    override var unit: String

    override var sigReceiveNodeSet: MutableSet<String>

    // ----------------------- 自定义属性 ---------------------
    override var attributeValueMap: MutableMap<String, DbcAttributeData>


    override var originalOwnerType: KClass<*>?
    override var originalOwner: Any?
    override var originalProperty: KProperty1<*, *>?
}