package io.github.shilic.smartDbc.dbc.dataModel.models

import io.github.shilic.smartDbc.dbc.attributes.models.DbcAttributeData
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import io.github.shilic.smartDbc.dbc.dataModel.contract.MutableCanSignal as MSig

/** 用于描述单个信号 */
@GridSheetBind(gridSheetType = GridSheetType.SubSignal)
open class CanSignalImp: MSig {
    // ----------------------- 基本信息 ----------------------
    @GridColumnBind(headerText = "SignalName", pattern = "信号名称|((Signal|signal|SIGNAL)\\s*(Name|name|NAME)\\s*)(?!([(（]?\\s*(Chinese|chinese|CHINESE)\\s*[）)]?))", valueType = GridValueType.Text, keyword = true)
    override var signalName: String =  ""
    @GridColumnBind(headerText = "SignalDescription", pattern = "SignalDescription|信号描述|信号注释|((Signal|signal|SIGNAL)\\s*(Name|name|NAME)\\s*[(（]?\\s*(Chinese|chinese|CHINESE)\\s*[）)]?)", valueType = GridValueType.Text)
    override var signalComment: String = ""

    // ----------------------- 信号排列 -------------------------
    @GridColumnBind(headerText = "GroupType ", pattern = "GroupType|分组类型", valueType = GridValueType.Custom, customAdapterName = "GroupType")
    override var groupType: MatrixGroupType = MatrixGroupType.DefaultGroup
    @GridColumnBind(headerText = "ByteOrder", pattern = "排列格式|ByteOrder|((Byte|byte|BYTE)\\s*(Order|order|ORDER))", valueType = GridValueType.Enumeration)
    override var byteOrder: CanByteOrder = CanByteOrder.Intel
    @GridColumnBind(headerText = "SignalSendType", pattern = "信号发送类型|SignalSendType|((Signal|signal|SIGNAL)\\s*(Send|send|SEND)\\s*(Type|type|TYPE))", valueType = GridValueType.Enumeration)
    override var genSigSendType: GenSigSendType = GenSigSendType.Cyclic
    @GridColumnBind(headerText = "StartBit", pattern = "起始位|StartBit|((Start|start|START)\\s*(Bit|bit|BIT))", valueType = GridValueType.NumberType)
    override var startBit: Int = 0
    @GridColumnBind(headerText = "BitLength", pattern = "信号长度|BitLength|((Bit|bit|BIT)\\s*(Length|length|LENGTH))", valueType = GridValueType.NumberType)
    override var bitLength: Int = 0
    @GridColumnBind(headerText = "DataType", pattern = "数据类型|DataType|((Data|data|DATA)\\s*(Type|type|TYPE))", valueType = GridValueType.Enumeration)
    override var dataType: CanDataType = CanDataType.Unsigned
    @GridColumnBind(headerText = "Factor", pattern = "精度|分辨率|Resolution|resolution|Factor|factor|FACTOR", valueType = GridValueType.NumberType)
    override var factor: Double = 1.0
    @GridColumnBind(headerText = "Offset", pattern = "偏移量|offset|Offset|OFFSET", valueType = GridValueType.NumberType)
    override var offset: Double = 0.0

    // ------------------------ 物理值 -----------------------
    @GridColumnBind(headerText = "SignalMinValuePhys",
        pattern = "物理最小值|SignalMinValuePhys|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Min|MIN|min)\\s*[.]?\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Phys|phys|PHYS|Phy|PHY|phy)\\s*[)）]?)",
        valueType = GridValueType.NumberType)
    override var signalMinValuePhys: Double = 0.0
    @GridColumnBind(headerText = "SignalMaxValuePhys",
        pattern = "物理最大值|SignalMaxValuePhys|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Max|MAX|max)\\s*[.]?\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Phys|phys|PHYS|Phy|PHY|phy)\\s*[)）]?)",
        valueType = GridValueType.NumberType)
    override var signalMaxValuePhys: Double = 0.0
    @GridColumnBind(headerText = "InitialValuePhys",
        pattern = "物理初始值|InitialValuePhys|((Initial|Initial|INITIAL|INI|Ini|ini|Init|INIT|init)\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Phys|phys|PHYS|Phy|PHY|phy)\\s*[)）]?)",
        valueType = GridValueType.NumberType)
    override var initialValuePhys: Double = 0.0

    // -------------- 原始值(总线值/未处理值) ----------------
    @GridColumnBind(headerText = "SignalMinValueHex", pattern = "总线最小值|SignalMinValueHex|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Min|MIN|min)\\s*[.]?\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Hex|hex|HEX)\\s*[)）]?)", valueType = GridValueType.NumberType)
    override var signalMinValueHex: Long = 0
    @GridColumnBind(headerText = "SignalMaxValueHex", pattern = "总线最大值|SignalMaxValueHex|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Max|MAX|max)\\s*[.]?\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Hex|hex|HEX)\\s*[)）]?)", valueType = GridValueType.NumberType)
    override  var signalMaxValueHex: Long = 0
    @GridColumnBind(headerText = "InitialValueHex", pattern = "总线初始值|InitialValueHex|((Initial|Initial|INITIAL|INI|Ini|ini|Init|INIT|init)\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Hex|hex|HEX)\\s*[)）]?)", valueType = GridValueType.NumberType)
    override var initialValueHex: Long = 0
    @GridColumnBind(headerText = "InvalidValueHex", pattern = "总线无效值|InvalidValueHex|((Invalid|INVALID|invalid)\\s*(Value|value|VALUE)\\s*[(（]?\\s*(Hex|hex|HEX)\\s*[)）]?)", valueType = GridValueType.HexNumber)
    override var invalidValueHex: Long = 0

    @GridColumnBind(headerText = "Unit", pattern = "单位|Unit|UNIT|unit", valueType = GridValueType.Text)
    override var unit: String = ""

    //@GridColumnBind(headerText = "信号接收节点列表", pattern = "信号接收节点列表", valueType = GridValueType.Custom , customAdapterName = "")
    override var sigReceiveNodeSet: MutableSet<String> = mutableSetOf()
    override var attributeValueMap: MutableMap<String, DbcAttributeData> = mutableMapOf()

    // ++++++++++++++++++ IGridRowData 接口实现 +++++++++++++++++
    override var gridFather: String = ""
    override var gridRowIndex: Int? = null

    // ++++++++++++ IValueTable (值描述接口) 实现 ++++++++++++++
    @GridColumnBind(headerText = "ValueTable", pattern = "值描述|ValueTable|((value|Value|VALUE)\\s*(TABLE|Table|table))|((Signal|signal|SIGNAL|sig|Sig|SIG)\\s*(Value|value|VALUE)\\s*(Description|description))", valueType = GridValueType.Text)
    override var valueTable: MutableMap<Int, String> = mutableMapOf()
    @Transient
    override var aValue: String = ""

    override fun toString(): String = baseInfo

    // ++++++++++++++++++ 实现 ISignalValue 接口, 用于直接设置DBC对象的值 ++++++++++++++++++
    /** 私有属性存储实际 物理值 */
    @Transient
    private var mCurrentPhyValue: Double = 0.0
    /** 私有属性存储实际 总线值 */
    @Transient
    private var mCurrentHexValue: Long = 0L
    /** 私有属性存储实际 文本值 */
    @Transient
    private var mCurrentTextValue: String = ""

    override var currentPhyValue: Double
        get() = mCurrentPhyValue
        set(value) {
            if (mCurrentPhyValue == value ) { return }
            mCurrentPhyValue = value
            mCurrentHexValue = phyToHex(value)
            mCurrentTextValue = phyToText(value)
            aValue = valueTable[mCurrentHexValue.toInt()] ?: ""
        }
    override var currentHexValue: Long
        get() = mCurrentHexValue
        set(value) {
            if (mCurrentHexValue == value) { return }
            mCurrentHexValue = value
            mCurrentPhyValue = hexToPhy(value)
            mCurrentTextValue = hexToText(value)
            aValue = valueTable[mCurrentHexValue.toInt()] ?: ""
        }
    override var currentTextValue: String
        get() = mCurrentTextValue
        set(value) {
            if (mCurrentTextValue == value){ return }
            mCurrentTextValue = value
            mCurrentPhyValue = textToPhy(value)
            mCurrentHexValue = textToHex(value)
            aValue = valueTable[mCurrentHexValue.toInt()] ?: ""
        }

    // ++++++++++ 实现 KPropertyAccessor 接口，用于从外部绑定对象获取值 +++++++++++
    @Transient
    override var originalOwnerType: KClass<*>? = null
    @Transient
    override var originalOwner: Any? = null
    @Transient
    override var originalProperty: KProperty1<*, *>? = null
}
