package io.github.shilic.smartDbc.dbc.io.writer

import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan

/** 将DBC对象一整个以序列方式输出 */
val DataBaseCan.allSequence: Sequence<String> get() = sequence {
    yieldAll(dbcTitleSequence)
    yieldAll(msgSequence)
    yieldAll(msgNodesSequence)
    yieldAll(commentSequence)
    yieldAll(attributeDefinitionSequence)
    yieldAll(attributeDefaultSequence)
    yieldAll(attributeValueSequence)
    yieldAll(valueTableSequence)
    yield("")
}

/** 1. 输出DBC标题序列:
 *
 * 包括：版本、通用模版、波特率、DBC节点
 * */
val DataBaseCan.dbcTitleSequence: Sequence<String> get() = sequence {
    yield(versionLine)
    yieldAll(dbcTemplateSequence)
    yield(nodesLine)
    yield("")
}
/** 输出DBC模板序列:
 *
 * 例如： [DbcTemplateText]
 * */
val dbcTemplateSequence: Sequence<String> get() = sequence {
    yieldAll(DbcTemplateText.split("\n"))
}
/** DBC模板常量;
 *
 * 以 'NS_ :' 和 'BS_:' 包裹起来的值
 * */
const val DbcTemplateText = """
NS_ : 
	NS_DESC_
	CM_
	BA_DEF_
	BA_
	VAL_
	CAT_DEF_
	CAT_
	FILTER
	BA_DEF_DEF_
	EV_DATA_
	ENVVAR_DATA_
	SGTYPE_
	SGTYPE_VAL_
	BA_DEF_SGTYPE_
	BA_SGTYPE_
	SIG_TYPE_REF_
	VAL_TABLE_
	SIG_GROUP_
	SIG_VALTYPE_
	SIGTYPE_VALTYPE_
	BO_TX_BU_
	BA_DEF_REL_
	BA_REL_
	BA_DEF_DEF_REL_
	BU_SG_REL_
	BU_EV_REL_
	BU_BO_REL_
	SG_MUL_VAL_

BS_:
"""

/** 2. 输出报文及信号的序列:
 *
 * 例如：
 *
 * BO_ 2560107544 CCSToAC1: 8 CCS
 *
 *  SG_ CCSToAC1_FactoryID : 0|8@1+ (1,0) [0|255] ""  AC
 *
 *  SG_ CCSToAC1_AirSw : 8|2@1+ (1,0) [0|3] ""  AC
 *
 *  SG_ CCSToCabin1_ColdGearReq : 10|4@1+ (1,0) [0|15] ""  AC
 *
 *  SG_ CCSToAC1_FanGearReq : 14|4@1+ (1,0) [0|15] ""  AC
 *
 *  SG_ heart : 56|8@1+ (1,0) [0|255] ""  AC
 * */
val DataBaseCan.msgSequence: Sequence<String> get() = sequence {
    msgMap.values.forEach { msg ->
        yield(msg.dbcValue)
        msg.signalMap.values.forEach { signal ->
            yield(signal.dbcValue)
        }
        yield("")
    }
    yield("")
}
/** 3. 输出报文节点序列:
 *
 * 使用 BO_TX_BU_ 创建一个报文节点;
 *
 * 例如：
 *
 * BO_TX_BU_ 2560107544 : Cabin,Test;
 * */
val DataBaseCan.msgNodesSequence : Sequence<String> get() = sequence {
    msgMap.values.forEach { msg ->
        if (msg.msgReceiveNodeSet.isNotEmpty()) {
            yield(msg.nodesLine)
        }
    }
    yield("")
}
/** 4. 输出注释序列:
 *
 * 使用 CM_ 关键字注释一个对象;
 *
 * 例如：
 *
 * CM_ BO_ 2560107544 "报文的注释";
 *
 * CM_ SG_ 2560107544 CCSToAC1_AirSw "空调开关。;
 *
 * CM_ SG_ 2560107544 CCSToCabin1_ColdGearReq "制冷档位请求。";
 *
 * CM_ SG_ 2560107544 CCSToAC1_FanGearReq "鼓风机档位大小请求。";
 *
 * CM_ BO_ 2560104484 "上装发给中控屏1(发给网关，网关转给中控屏)。";
 *
 * CM_ SG_ 2560104484 CabinToCCS1_FactoryID "工厂代号。";
 *  */
val DataBaseCan.commentSequence : Sequence<String> get() = sequence {
    // TODO 需要新增节点的注释, 新增节点的数据结构
    msgMap.values.forEach { msg ->
        if (msg.msgComment.isNotBlank()) {
            yield(msg.commentLine)
        }
        msg.signalMap.values.forEach { signal ->
            if (signal.signalComment.isNotBlank()) {
                yield(signal.commentLine)
            }
        }
    }
    yield("")
}
/** 5. 输出自定义属性定义 序列:
 *
 * 使用 BA_DEF_ 关键字定义一个自定义属性;
 *
 * 例如：
 *
 * BA_DEF_ BO_  "New_AttrDef_12_Double" FLOAT 0 0;
 *
 * BA_DEF_ SG_  "GenSigStartValue" INT 0 65535;
 *
 * BA_DEF_ SG_  "GenSigSendType" ENUM  "Cyclic","OnWrite","OnWriteWithRepetition","OnChange","OnChangeWithRepetition","IfActive","IfActiveWithRepetition","NoSigSendType";
 *
 * BA_DEF_ SG_  "GenSigInactiveValue" INT 0 65535;
 *
 * BA_DEF_ BO_  "GenMsgCycleTime" INT 0 65535;
 *
 * BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
 *
 * BA_DEF_ BO_  "GwUsedMsg" ENUM  "No","Yes";
 *
 * BA_DEF_ BO_  "DiagState" ENUM  "No","Yes";
 *
 * BA_DEF_ BO_  "NmMessage" ENUM  "No","Yes";
 *
 * BA_DEF_ BU_  "NmStationAddress" HEX 0 0;
 *
 * BA_DEF_  "DBName" STRING ;
 *
 * BA_DEF_  "BusType" STRING ;
 *
 * */
val DataBaseCan.attributeDefinitionSequence : Sequence<String> get() = sequence {
    attributeMap.values.forEach { attributeDefinition ->
        yield(attributeDefinition.dbcValue)
    }
}
/** 6. 输出自定义属性默认值序列:
 *
 * 使用 BA_DEF_DEF_ 关键字定义一个自定义属性的默认值;
 *
 * 例如：
 *
 * BA_DEF_DEF_  "New_AttrDef_12_Double" 0;
 *
 * BA_DEF_DEF_  "GenSigStartValue" 0;
 *
 * BA_DEF_DEF_  "GenSigSendType" "Cyclic";
 *
 * BA_DEF_DEF_  "GenSigInactiveValue" 0;
 *
 * BA_DEF_DEF_  "GenMsgCycleTime" 200;
 *
 * BA_DEF_DEF_  "GenMsgSendType" "Cyclic";
 *
 * BA_DEF_DEF_  "GwUsedMsg" "No";
 *
 * BA_DEF_DEF_  "DiagState" "No";
 *
 * BA_DEF_DEF_  "NmMessage" "No";
 *
 * BA_DEF_DEF_  "NmStationAddress" 0;
 *
 * BA_DEF_DEF_  "DBName" "石李城";
 *
 * BA_DEF_DEF_  "BusType" "CAN";
 * */
val DataBaseCan.attributeDefaultSequence : Sequence<String> get() = sequence {
    attributeMap.values.forEach { attributeDefinition ->
        yield(attributeDefinition.defaultValueLine)
    }
}
/** 7. 输出自定义属性值序列:
 *
 * 使用 BA_ 关键字定义一个自定义属性值;
 *
 * 例如：
 *
 * BA_ "DBName" "Example";
 *
 * BA_ "NmMessage" BO_ 2560107544 0;
 *
 * BA_ "DiagState" BO_ 2560107544 0;
 *
 * BA_ "GwUsedMsg" BO_ 2560107544 0;
 *
 * BA_ "GenMsgCycleTime" BO_ 2560107544 500;
 *
 * BA_ "GenMsgSendType" BO_ 2560107544 1;
 *
 * BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_FactoryID 0;
 *
 * BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_AirSw 0;
 *
 * BA_ "GenSigStartValue" SG_ 2560107544 CCSToCabin1_ColdGearReq 0;
 *
 * BA_ "GenSigStartValue" SG_ 2560107544 CCSToAC1_FanGearReq 0;
 *
 * BA_ "GenSigStartValue" SG_ 2560107544 heart 0;
 *
 * BA_ "GenSigStartValue" SG_ 2560104484 CabinToCCS1_FactoryID 0;
 */
val DataBaseCan.attributeValueSequence : Sequence<String> get() = sequence {
    // 1. 先输出DBC级别的自定义属性值, 也就是 作用域 Net 类型的
    attributeValueMap.values.forEach { attributeValue ->
        yield(attributeValue.dbcValue)
    }
    // TODO 2. 再输出节点级别的自定义属性值。

    // 3. 再输出报文级别的自定义属性值。
    msgMap.values.forEach { msg ->
        msg.attributeValueMap.values.forEach { attributeValue ->
            yield(attributeValue.dbcValue)
        }
    }
    // 4. 最后输出信号级别的自定义属性值。
    msgMap.values.forEach { msg ->
        msg.signalMap.values.forEach { signal ->
            signal.attributeValueMap.values.forEach { attributeValue ->
                yield(attributeValue.dbcValue)
            }
        }
    }
}
/** 8. 输出值描述序列：
 *
 * 使用 VAL_ 创建一个值描述;
 *
 * 例如：
 *
 * VAL_ 2560104484 CabinToCCS1_FanMotFlt 0 "无故障，嘟嘟嘟" 1 "短路" 2 "断路" 3 "堵转" ;
 *
 * VAL_ 2560104484 CabinToCCS1_FanGearSts 15 "无效" 14 "预留" 13 "预留" 12 "预留" 11 "预留" 10 "预留" 9 "预留" 8 "八" 7 "七" 6 "六" 5 "五" 4 "四" 3 "三" 2 "二" 1 "一" 0 "预留" ;
 *
 * VAL_ 2560104484 CabinToCCS1_ColdMotFlt 3 "堵转" 2 "断路" 1 "短路" 0 "无故障" ;
 *
 * VAL_ 2560104484 CabinToCCS1_AirSts 3 "无效值未使用" 2 "开启" 1 "关闭" 0 "预留" ;
 *
 * */
val DataBaseCan.valueTableSequence : Sequence<String> get() = sequence {
    msgMap.values.forEach { msg ->
        msg.signalMap.values.forEach { signal ->
            if (signal.valueTable.isNotEmpty()) {
                yield(signal.valueTableLine)
            }
        }
    }
}
