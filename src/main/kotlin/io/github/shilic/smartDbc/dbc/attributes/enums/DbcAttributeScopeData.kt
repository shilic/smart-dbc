package io.github.shilic.smartDbc.dbc.attributes.enums

import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.IDbcElement

/** 自定义属性作用域数据
 *
 * 作用域数据，用于生成DBC文件的属性作用域数据;
 *
 * [DbcAttributeScopeData.Net] 网络类型, 在DBC文件中的编码类似 BA_ "DBName" "Example";, 表示整个DBC文件的自定义属性;
 *
 * [DbcAttributeScopeData.Message] 报文类型, 在DBC文件中的编码类似 BA_ "GenMsgCycleTime" BO_ 2560107544 500;
 *
 * [DbcAttributeScopeData.Signal] 信号类型, 在DBC文件中的编码类似 BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_FactoryID 0;
 *
 * [DbcAttributeScopeData.Node] 节点类型, 在DBC文件中的编码类似  BA_ "New_AttrDef_14" BU_ CCS 3.14159;
 *
 * */
sealed class DbcAttributeScopeData : IDbcElement {
    /** 自定义属性的作用域类型
     *
     * [DbcAttributeScopeDefinition.Net] 网络类型, 在DBC文件中的编码为 空字符串 "" , 表示整个DBC文件的自定义属性;
     *
     * [DbcAttributeScopeDefinition.Message] 报文类型, 在DBC文件中的编码为 BO_
     *
     * [DbcAttributeScopeDefinition.Signal] 信号类型, 在DBC文件中的编码为 SG_
     *
     * [DbcAttributeScopeDefinition.Node] 节点类型, 在DBC文件中的编码为 BU_
     * */
    abstract val scope: DbcAttributeScopeDefinition
    override val dbcKey: String get() = scope.dbcKey
    override fun toString(): String = "${DbcAttributeScopeData::class.simpleName}.${this::class.simpleName}" +
            "(${::scope.name}=$scope,${::dbcValue.name}=$dbcValue)"

    /** 作用域的DBC数据，用于生成DBC文件的属性作用域数据;
     *
     * 类似：
     *
     * BO_ 2560107544
     *
     * SG_ 2560107544 CCSToAC1_FactoryID
     *
     * BU_ CCS
     *
     * 网络类型则直接输出空字符串
     * */
    abstract override val dbcValue: String
    /** [DbcAttributeScopeData.Net] 网络类型, 在DBC文件中的编码为 空字符串 "" , 表示整个DBC文件的自定义属性;
     *
     * 例如 DB 的名称 DBName ; 例如 总线类型 BusType ;
     *
     * BA_ "DBName" "Example"; */
    /* 使用 object 类型，序列化会出现问题；所以这里我使用了class类型，确保序列化密封类时没有问题 */
    class Net: DbcAttributeScopeData() {
        override val scope: DbcAttributeScopeDefinition = DbcAttributeScopeDefinition.Net
        override val dbcValue: String get() = ""
    }
    /** [DbcAttributeScopeData.Message] 报文类型;
     *
     * 在DBC文件中的编码类似:  BA_ "GenMsgCycleTime" BO_ 2560107544 500; */
    data class Message(val longIdCode: Long) : DbcAttributeScopeData() {
        override val scope: DbcAttributeScopeDefinition = DbcAttributeScopeDefinition.Message
        override val dbcValue: String get() = "$BO_ $longIdCode "
    }
    /** [DbcAttributeScopeData.Signal] 信号类型;
     *
     * 在DBC文件中的编码类似: BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_FactoryID 0; */
    data class Signal(val longIdCode: Long, val signalName: String) : DbcAttributeScopeData() {
        override val scope: DbcAttributeScopeDefinition = DbcAttributeScopeDefinition.Signal
        override val dbcValue: String get() = "$SG_ $longIdCode $signalName "
    }
    /** [DbcAttributeScopeData.Node] 节点类型;
     *
     * 在DBC文件中的编码类似  BA_ "New_AttrDef_14" BU_ CCS 3.14159; */
    data class Node(val nodeName: String) : DbcAttributeScopeData() {
        override val scope: DbcAttributeScopeDefinition = DbcAttributeScopeDefinition.Node
        override val dbcValue: String get() = "$BU_ $nodeName "
    }

}