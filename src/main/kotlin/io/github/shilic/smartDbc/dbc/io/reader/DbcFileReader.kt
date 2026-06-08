package io.github.shilic.smartDbc.dbc.io.reader

import io.github.shilic.smartDbc.common.typeExtension.*
import io.github.shilic.smartDbc.dbc.attributes.enums.*
import io.github.shilic.smartDbc.dbc.attributes.models.*
import io.github.shilic.smartDbc.dbc.attributes.enums.DbcAttributeValueType.*
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.models.*

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/**
 * DBC文件解析器。通过.dbc后缀的文件, 解析为DBC对象。
 */
class DbcFileReader {
    // --------------------- 文件基本信息 ----------------------
    /** 传入文件流 */
    private val inputStream: FileInputStream
    /** 是否需要关闭文件 */
    private val closeFile: Boolean

    // +++++++++++++++++++++ 可选参数 +++++++++++++++++++++++++
    /** 打开文件时的编码; 默认 Charset.forName("GBK") */
    val encoding: Charset

    // ---------------- java风格构造函数, 用于精确控制构造行为 ----------------
    /** 直接通过文件输入流的方式初始化一个 'DBC文件解析器'
     *
     * 此时不需要自动关闭文件输入流，由外部程序自行关闭文件。
     * */
    @Suppress("UNUSED")
    constructor(inputStream: FileInputStream) {
        this.inputStream = inputStream
        this.closeFile = false
        this.encoding = inputStream.encoding ?: Charset.forName("GBK")
    }
    /** 通过文件路径初始化一个 'DBC文件解析器'
     *
     * 此时会自动关闭文件输入流。
     * */
    constructor(filePath: String) {
        val file = File(filePath)
        require(file.exists()) { "${DbcFileReader::class.simpleName}：文件\"${file.name}\"不存在" }
        require(file.isFile) { "${DbcFileReader::class.simpleName}：确保\"${file.name}\"是文件，而不是目录" }
        require(file.name.lowercase().endsWith(".dbc")) { "${DbcFileReader::class.simpleName}：文件\"${file.name}\"后缀名必须是 .dbc " }
        this.inputStream = file.inputStream()
        this.closeFile = true
        this.encoding = inputStream.encoding ?: Charset.forName("GBK")
    }

    // --------------------- 正则表达式们 ----------------------
    /** 匹配标题的正则表达式。*/
    private val startRegex = Regex("""^(?<start>${VERSION}|${BU_colon}|${BO_}|${SG_}|${BO_TX_BU_}|${CM_}|${BA_DEF_}|${BA_DEF_DEF_}|${BA_}|${VAL_})\s+""")
    /** 版本行正则表达式。*/
    private val versionRegex = Regex("""^${VERSION}\s+"(?<version>[^"]*)"""")
    /** 解析信号的正则表达式。
     *
     * 解析形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS  */
    private val sgRegex = Regex(
        """${SG_}\s+(?<sigName>\S+)\s+(?<group>[mM]\d*)?\s*:""" +
        """\s*(?<startBit>\d+)\s*[|]\s*(?<bitLength>\d+)\s*@\s*(?<ByteOrder>[10])\s*(?<DataType>[+-])""" +
        """\s*\(\s*(?<Factor>-?\d[\d.]*)\s*,\s*(?<Offset>-?\d[\d.]*)\s*\)\s*\[\s*(?<min>-?\d[\d.]*)\s*[|]\s*(?<max>-?\d[\d.]*)]\s*""" +
        """"(?<unit>[^"]*)"\s*(?<nodeSet>[\w,]*)?"""
    )
    /** 解析消息的正则表达式。
     *
     * 解析形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    private val boRegex = Regex("""${BO_}\s+(?<longIdCode>\d+)\s+(?<msgName>[a-zA-Z_]\w*)\s*:\s*(?<length>\d+)\s*(?<node>[a-zA-Z_]\w*)?""")
    /** 解析节点的正则表达式。
     *
     * 解析形如 BU_: CCS AC
     *
     * */
    private val nodeRegex = Regex("""${BU_colon}(?<nodeSet>[\w\s]*)?""")
    /** 解析消息接收节点的行正则表达式。
     *
     * 解析形如: BO_TX_BU_ 2560107544 : Cabin,Test;
     * */
    private val msgReceiveNodesRegex = Regex("""$BO_TX_BU_\s+(?<longIdCode>\d+)\s*:\s*(?<nodeSet>[\w,]*)?\s*;""")
    /** 解析注释的正则表达式。
     *
     * 解析形如
     *
     * CM_ BO_ 2560107544 "报文的注释";
     * */
    private val msgCommentRegex = Regex("""$BO_\s+(?<longIdCode>\d+)\s+"(?<comment>[^"]*)"\s*;""")
    /** 解析注释的正则表达式。
     *
     * 解析形如
     *
     * CM_ SG_ 2560107544 CCSToAC1_AirSw "空调开关。";
     * */
    private val sigCommentRegex = Regex("""$SG_\s+(?<longIdCode>\d+)\s+(?<sigName>[a-zA-Z_]\w*)\s+"(?<comment>[^"]*)"\s*;""")
    /** 解析属性定义的正则表达式。
     *
     * 解析形如
     *
     * BA_DEF_ SG_  "GenSigStartValue" INT 0 65535;
     *
     * BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
     *
     * BA_DEF_ BU_  "New_AttrDef_14" FLOAT 0 0;
     *
     * BA_DEF_ EV_  "New_AttrDef_17" INT 0 0;
     *
     * BA_DEF_  "DBName" STRING ;
     *
     * BA_DEF_  "BusType" STRING ;
     * */
    private val baDefRegex = Regex("""${BA_DEF_}\s+(?<scope>$BO_|$SG_|$BU_|$EV_)?\s*"(?<name>[^"]+)"\s+(?<valueType>${INT}|${FLOAT}|${STRING}|${ENUM}|${HEX})(?<range>.*);""")
    /** 解析数值类型的自定义属性范围的正则表达式。
     *
     * 例如： BA_DEF_ SG_  "GenSigInactiveValue" INT -5 65535; 中的范围
     * */
    private val baDefMinMaxRegex = Regex("""(?<min>\S+)\s+(?<max>\S+)""")
    /** 解析自定义属性默认值的正则表达式
     *
     * 解析形如：
     *
     * BA_DEF_DEF_  "GenSigStartValue" 0;
     *
     * BA_DEF_DEF_  "GenMsgCycleTime" 200;
     *
     * BA_DEF_DEF_  "GenMsgSendType" "Cyclic";
     *
     * BA_DEF_DEF_  "GwUsedMsg" "No";
     *
     * BA_DEF_DEF_  "DiagState" "No";
     *
     * BA_DEF_DEF_  "NmStationAddress" 0;
     *
     * BA_DEF_DEF_  "BusType" "CAN";
     *
     * */
    private val baDefDefaultRegex = Regex("""${BA_DEF_DEF_}\s+"(?<name>[^"]+)"(?<value>.*);""")

    /** 主函数: 解析 DBC;
     *
     * 并自动判断是否应该关闭文件 */
    fun createMutableDbc(): DataBaseCanImp  = parseLines(inputStream.reader(encoding).buffered()).also { if (closeFile) { inputStream.close() } }
    /** 逐行解析 */
    private fun parseLines(reader: BufferedReader) : DataBaseCanImp  {
        val dbc = DataBaseCanImp()
        // 行号，调试用
        var lineNumber = 0
        // 使用forEachLine自动处理资源关闭
        reader.useLines { lines ->
            lines.forEach { rawLine ->
                lineNumber += 1
                val line = rawLine.trim()
                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) { return@forEach }

                // 跳过匹配失败的行
                val lineStart = startRegex.find(line)?.groups["start"]?.value ?: return@forEach
                try {
                    when (lineStart) {
                        VERSION  -> dbc.version = parseVersion(line)
                        BU_colon -> dbc.nodeSet.addAll(parseBU(line))
                        BO_ -> dbc.set(parseBO(line))
                        SG_ -> dbc.getMsgAt(dbc.msgMap.size - 1)?.set(parseSG(line))
                        BO_TX_BU_ -> {
                            val (msgId, nodeSet) = parseBOTXBU(line)
                            dbc[msgId]?.msgReceiveNodeSet?.addAll(nodeSet)
                        }
                        CM_ -> parseCM(line, dbc)
                        BA_DEF_ -> {
                            val attribute = parseBaDef(line)
                            dbc.attributeMap[attribute.dbcKey] = attribute
                        }
                        BA_DEF_DEF_ -> parseBaDefault(line, dbc)
                        BA_ -> {
                        }
                        VAL_ -> {
                        }
                    }
                }
                catch (exception: Exception){
                    error("${DbcFileReader::class.simpleName}报错：${exception.message} 。错误行号: $lineNumber, 行内容: $line ")
                }
            }
        }
        return dbc
    }
    /** 解析版本行
     *
     * 形如 VERSION "V1.0.1"
     * */
    fun parseVersion(rawLine: String): String {
        val line = rawLine.trim()
        require(line.startsWith(VERSION)) { "该行不以 '${VERSION}' 开头" }
        val matchGroups = versionRegex.find(line)?.groups ?: error("正则表达式识别异常")
        return matchGroups["version"]?.value ?: ""
    }
    /** 解析信号行
     *
     * 形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS
     * */
    fun parseSG(rawLine: String): CanSignalImp {
        val line = rawLine.trim()
        require(line.startsWith(SG_)) { "该行不以 '${SG_}' 开头;" }
        val matchGroups = sgRegex.find(line)?.groups ?: error("正则表达式识别异常")
        return CanSignalImp().apply {
            signalName = matchGroups["sigName"]!!.value.also { it.requireWord() }
            groupType = MatrixGroupType.createBy(matchGroups["group"]?.value ?: "")
            startBit = matchGroups["startBit"]!!.value.toInt()
            bitLength = matchGroups["bitLength"]!!.value.toInt()
            byteOrder = CanByteOrder.createBy(matchGroups["ByteOrder"]!!.value)
            dataType = CanDataType.createBy(matchGroups["DataType"]!!.value)
            factor = matchGroups["Factor"]!!.value.toDouble()
            offset = matchGroups["Offset"]!!.value.toDouble()
            signalMinValuePhys = matchGroups["min"]!!.value.toDouble()
            signalMaxValuePhys = matchGroups["max"]!!.value.toDouble()
            unit = matchGroups["unit"]?.value ?: ""
            sigReceiveNodeSet = matchGroups["nodeSet"]?.value?.split(",")
                ?.also { list -> list.forEach { it.trim().requireWord() } }?.toMutableSet() ?: mutableSetOf()
        }
    }
    /** 解析消息行
     *
     * 形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    fun parseBO(rawLine: String): CanMessageImp {
        val line = rawLine.trim()
        require(line.startsWith(BO_)) { "该行不以 '${BO_}' 开头;" }
        val matchGroups  = boRegex.find(line)?.groups ?: error("正则表达式识别异常")
        return CanMessageImp().apply {
            val longIdCode : Long = matchGroups["longIdCode"]!!.value.toLong(10)
            msgIdType = CanExternFlag.createByLongIdCode(longIdCode)
            msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
            msgName = matchGroups["msgName"]!!.value.trim()
            msgLength = matchGroups["length"]!!.value.toInt()
            nodeName = matchGroups["node"]?.value?.trim() ?: Vector__XXX
        }
    }
    /** 解析节点行
     *
     * 形如: BU_: CCS AC
     * */
    fun parseBU(rawLine: String): Set<String> {
        val line = rawLine.trim()
        require(line.startsWith(BU_colon)) { "该行不以 '${BU_colon}' 开头" }
        val matchGroups = nodeRegex.find(line)?.groups ?: error("正则表达式识别异常")
        return matchGroups["nodeSet"]?.value?.split(" ")
            ?.also { list -> list.forEach { it.trim().requireWord() } }?.toSet() ?: emptySet()
    }
    /** 解析消息接收节点行
     *
     * 形如: BO_TX_BU_ 2560107544 : Cabin,Test;
     * */
    fun parseBOTXBU(rawLine: String): Pair<Int, Set<String>> {
        val line = rawLine.trim()
        require(line.startsWith(BO_TX_BU_)) { "该行不以 '${BO_TX_BU_}' 开头" }
        val matchGroups = msgReceiveNodesRegex.find(line)?.groups ?: error("正则表达式识别异常")
        val longIdCode : Long = matchGroups["longIdCode"]!!.value.toLong(10)
        val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
        val nodeSet = matchGroups["nodeSet"]?.value?.split(",")
            ?.also { list -> list.forEach { it.trim().requireWord() } }?.toSet() ?: emptySet()
        return  msgId to nodeSet
    }
    /** 解析注释行
     *
     * CM_ BU_ CCS "大屏节点";
     *
     * CM_ BO_ 2560107544 "报文的注释";
     *
     * CM_ SG_ 2560107544 CCSToAC1_AirSw "空调开关。";
     * */
    fun parseCM(rawLine: String, dbc : DataBaseCanImp) {
        val line = rawLine.trim()
        require(line.startsWith(CM_)) { "该行不以 '${CM_}' 开头" }
        val mLine = line.removePrefix(CM_).trim()
        when {
            mLine.startsWith(BO_) -> {
                val matchResult = msgCommentRegex.find(mLine) ?: error("正则表达式识别异常")
                val longIdCode : Long = matchResult.groups["longIdCode"]!!.value.toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val comment = matchResult.groups["comment"]?.value ?: ""
                dbc[msgId]?.msgComment = comment
            }
            mLine.startsWith(SG_) -> {
                val matchResult = sigCommentRegex.find(mLine) ?: error("正则表达式识别异常")
                val longIdCode : Long = matchResult.groups["longIdCode"]!!.value.toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val sigName = matchResult.groups["sigName"]!!.value
                val comment = matchResult.groups["comment"]?.value ?: ""
                dbc[msgId]?.get(sigName)?.signalComment = comment
            }
            // 添加其他注释的解析......
        }
    }
    /** 解析属性定义
     *
     * 解析形如
     *
     * BA_DEF_ BO_  "New_AttrDef_12_Double" FLOAT 0 0;
     *
     * BA_DEF_ SG_  "GenSigStartValue" INT 0 65535;
     *
     * BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
     *
     * BA_DEF_ BU_  "New_AttrDef_14" FLOAT 0 0;
     *
     * BA_DEF_ BU_  "NmStationAddress" HEX 0 15;
     *
     * BA_DEF_ EV_  "New_AttrDef_17" INT 0 0;
     *
     * BA_DEF_  "DBName" STRING ;
     *
     * BA_DEF_  "BusType" STRING ;
     * */
    fun parseBaDef(rawLine: String) : DbcAttributeDefinitionImp {
        val line = rawLine.trim()
        require(line.startsWith(BA_DEF_)) { "该行不以 '${BA_DEF_}' 开头" }
        val matchGroups = baDefRegex.find(line)?.groups ?: error("正则表达式识别异常")
        val valueType : DbcAttributeValueType = DbcAttributeValueType.createBy(matchGroups["valueType"]!!.value)
        // 取最后一个分号之前，到数值类型之后的所有字符，视为属性范围; 再去除空白字符，过滤空白字符。
        val range: String? = matchGroups["range"]?.value?.trim()
        val attribute = DbcAttributeDefinitionImp().apply {
            this.scope = DbcAttributeScopeDefinition.createBy(matchGroups["scope"]?.value ?: "")
            this.name = matchGroups["name"]!!.value.also { it.requireWord() }
            this.valueType = valueType
        }
        // 添加属性范围(过滤掉空白字符)
        range?.takeIf { it.isNotBlank() }?.let {
            when (valueType) {
                StringType -> Unit
                // 解析形如: BA_DEF_ SG_  "GenSigInactiveValue" INT -5 65535;
                IntegerType, FloatType, HexType -> {
                    val groups = baDefMinMaxRegex.find(range)?.groups ?: error("正则表达式识别异常, 最大值最小值应该由一个空格分开两个值")
                    // 校验数值必须是数值类型，否则报错
                    attribute.min = groups["min"]!!.value.also { it.requireDouble() }
                    attribute.max = groups["max"]!!.value.also { it.requireDouble()
                        require(it.toDouble() >= attribute.min.toDouble()){"最大值: '${it}' 不能小于最小值"}
                    }
                }
                // 解析形如： BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
                Enumeration -> {
                    attribute.valueTable = parseRangeToEnumMap(range)
                }
            }
        }
        return attribute
    }
    /** 将一串范围字符串经过校验后，转换为 MutableMap<Int, String>
     *
     * 元素必须双引号包裹, 并且不能重复, 且必须为单词类型
     *
     * 例如： "Cyclic","Event","IfActive","CE","CA";
     * */
    fun parseRangeToEnumMap(range: String) : MutableMap<Int, String> {
        return range.trim()
            // 以逗号分割元素，并且去除前后空格
            .split(",").map { it.trim() }
            .also { list ->
                // 校验所有元素必须以双引号开头和结尾
                list.forEach { it.requireStartsAndEnds("\"")}
                require(list.hasNoDuplicates()) { "自定义属性的枚举项不能重复"}
            }
            // 去除 双引号 和 左右的空白字符 ,
            .map { it.removeSurrounding("\"").trim() }
            .also {
                list -> list.forEach {
                    // 选项中不可以有空白字符，否则容易造成歧义
                    require(it.isNotBlank()) { "自定义属性的枚举项不能为空白字符, 否则容易造成歧义" }
                    // 校验每一个元素必须为单词类型
                    it.requireWord()
                }
            }
            // 给 List 集合添加序号，并且转换为MutableMap<Int, String>
            .withIndex().associateTo(mutableMapOf()) { (index, value) -> index to value }
    }
    /** 解析自定义属性的默认值
     *
     * 例如:
     *
     * BA_DEF_DEF_  "GenSigStartValue" 0;
     *
     * BA_DEF_DEF_  "GenMsgCycleTime" 200;
     *
     * BA_DEF_DEF_  "GenMsgSendType" "Cyclic";
     *
     * BA_DEF_DEF_  "GwUsedMsg" "No";
     *
     * BA_DEF_DEF_  "DiagState" "No";
     *
     * BA_DEF_DEF_  "NmStationAddress" 0;
     *
     * BA_DEF_DEF_  "BusType" "CAN";
     *
     * */
    fun parseBaDefault(rawLine: String, dbc : DataBaseCanImp)  {
        val line = rawLine.trim()
        require(line.startsWith(BA_DEF_DEF_)) { "该行不以 '${BA_DEF_DEF_}' 开头" }
        val matchGroups = baDefDefaultRegex.find(line)?.groups ?: error("正则表达式识别异常")
        val name = matchGroups["name"]!!.value
        val value = matchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性的默认值不可以为空")

        var attributeDefinition : DbcAttributeDefinitionImp = dbc.attributeMap[name] ?: error("在DBC中, 找不到属性定义: $name")
        attributeDefinition.defaultValue = when (attributeDefinition.valueType) {
            StringType -> value.also { it.requireStartsAndEnds("\"") } .removeSurrounding("\"").trim()
            IntegerType, FloatType, HexType -> value.trim().also { it.requireDouble() }
            Enumeration -> {
                value.also { it.requireStartsAndEnds("\"") } .removeSurrounding("\"").trim().also {
                    require(attributeDefinition.valueTable.values.contains(it)) {"自定义属性 '${attributeDefinition.name}' 的枚举项不存在: $value"}
                }
            }
        }
    }

    fun parseBaValue(rawLine: String, dbc : DataBaseCanImp) { 
    }


}