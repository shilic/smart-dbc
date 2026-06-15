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
import kotlin.collections.associateTo

/** DBC文件解析器。通过.dbc后缀的文件, 解析为DBC对象。 */
class DbcFileReader {
    /** 传入文件流 */
    val inputStream: FileInputStream
    /** 打开文件时的编码, 只读, 可根据文件自动判断应该用何种编码打开; 默认 Charset.forName("GBK")
     *
     * 此处代码是为了 解决 使用 TSMaster 打开 CANoe 编辑过的dbc文件导致的乱码问题; 因为 CANoe 使用 GBK 编码, 而 TSMaster 使用 UTF-8 编码。
     * */
    val encoding: Charset
    // ---------------- java风格构造函数, 用于精确控制构造行为 ----------------

    /** 通过文件路径初始化一个 'DBC文件解析器' */
    constructor(filePath: String) : this(File(filePath))
    /** 通过文件对象初始化一个 'DBC文件解析器' */
    constructor(file: File) {
        require(file.exists()) { "${DbcFileReader::class.simpleName}：文件\"${file.name}\"不存在" }
        require(file.isFile) { "${DbcFileReader::class.simpleName}：确保\"${file.name}\"是文件，而不是目录" }
        require(file.name.lowercase().endsWith(".dbc")) { "${DbcFileReader::class.simpleName}：文件\"${file.name}\"后缀名必须是 .dbc " }
        this.inputStream = file.inputStream()
        this.encoding = inputStream.encoding ?: Charset.forName("GBK")
    }
    /** 直接通过文件输入流的方式初始化一个 'DBC文件解析器' */
    constructor(inputStream: FileInputStream) {
        this.inputStream = inputStream
        this.encoding = inputStream.encoding ?: Charset.forName("GBK")
    }
    /** 主函数: 解析 DBC;
     *
     * 自动使用正确的文本文件编码; */
    fun create() : DataBaseCanImp = inputStream.reader(encoding).buffered().use { parseLines(it) }
    /** 匹配标题的正则表达式。*/
    val startRegex = Regex("""^(?<start>${VERSION}|${BU_colon}|${BO_}|${SG_}|${BO_TX_BU_}|${CM_}|${BA_DEF_}|${BA_DEF_DEF_}|${BA_}|${VAL_})\s+""")
    /** 逐行解析 */
    private fun parseLines(reader: BufferedReader): DataBaseCanImp {
        val dbc = DataBaseCanImp()
        // 行号，调试用; 这里需要从0开始, 因为进去之后首先会+1
        var lineNumber = 0
        // 使用forEachLine自动处理资源关闭, 将文本文件转换为字符串序列
        reader.forEachLine { rawLine ->
            lineNumber += 1
            val line = rawLine.trim()
            // 跳过空行和注释
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) { return@forEachLine }
            // 跳过匹配失败的行
            val lineStart = startRegex.find(line)?.groups["start"]?.value ?: return@forEachLine
            try {
                when (lineStart) {
                    VERSION  -> dbc.version = parseVersion(line)
                    BU_colon -> dbc.nodeSet.addAll(parseBU(line))
                    BO_ -> {
                        val msg = parseBO(line)
                        dbc.set(msg)
                    }
                    SG_ -> dbc.getMsgAt(dbc.msgMap.size - 1)?.let { it.set( parseSG(line).apply { longIdCode = it.longIdCode }) }
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
                    BA_ -> parseBaValue(line, dbc)
                    VAL_ -> parseValueTable(line, dbc)
                }
            }
            catch (exception: Exception) {
                error("${DbcFileReader::class.simpleName}报错：${exception.message} 。错误行号: $lineNumber, 行内容: $line ")
            }
        }
        return dbc
    }
    /** 版本行正则表达式。*/
    val versionRegex = Regex("""^${VERSION}\s+"(?<version>[^"]*)"""")
    /** 解析版本行
     *
     * 形如 VERSION "V1.0.1"
     * */
    fun parseVersion(rawLine: String): String {
        val line = rawLine.trim()
        require(line.startsWith(VERSION)) { "该行不以 '${VERSION}' 开头" }
        val matchGroups = versionRegex.find(line)?.groups ?: error("识别版本的正则表达式识别异常")
        return matchGroups["version"]?.value?.trim() ?: ""
    }
    /** 解析信号的正则表达式。
     *
     * 解析形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS  */
    val sgRegex = Regex(
        """${SG_}\s+(?<sigName>\S+)\s+(?<group>[mM]\d*)?\s*:""" +
                """\s*(?<startBit>\S+)\s*[|]\s*(?<bitLength>\S+)\s*@\s*(?<byteOrder>[10])\s*(?<dataType>[+-])""" +
                """\s*\(\s*(?<factor>\S+)\s*,\s*(?<offset>\S+)\s*\)\s*\[\s*(?<min>\S+)\s*[|]\s*(?<max>\S+)\s*]\s*""" +
                """"(?<unit>[^"]*)"\s*(?<nodeSet>.*)"""
    )
    /** 解析信号行
     *
     * 形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS
     * */
    fun parseSG(rawLine: String): CanSignalImp {
        val line = rawLine.trim()
        require(line.startsWith(SG_)) { "该行不以 '${SG_}' 开头;" }
        val matchGroups = sgRegex.find(line)?.groups ?: error("识别信号的正则表达式识别异常")
        return CanSignalImp().apply {
            signalName = matchGroups["sigName"]!!.value.trim().also { it.requireWord() }
            groupType = MatrixGroupType.createBy(matchGroups["group"]?.value?.trim() ?: "")
            startBit = matchGroups["startBit"]!!.value.trim().also { it.requireDecimal() }.toInt()
            bitLength = matchGroups["bitLength"]!!.value.trim().also { it.requireDecimal() }.toInt()
            byteOrder = CanByteOrder.createBy(matchGroups["byteOrder"]!!.value.trim())
            dataType = CanDataType.createBy(matchGroups["dataType"]!!.value.trim())
            factor = matchGroups["factor"]!!.value.trim().also { it.requireDouble() }.toDouble()
            offset = matchGroups["offset"]!!.value.trim().also { it.requireDouble() }.toDouble()
            signalMinValuePhys = matchGroups["min"]!!.value.trim().also { it.requireDouble() }.toDouble()
            signalMaxValuePhys = matchGroups["max"]!!.value.trim().also { it.requireDouble() }.toDouble()
            unit = matchGroups["unit"]?.value ?: ""
            sigReceiveNodeSet = matchGroups["nodeSet"]?.value?.trim()?.split(",")
                ?.also { list -> list.forEach { it.trim().requireWord() } }?.map { it.trim() }?.toMutableSet() ?: mutableSetOf()
        }
    }
    /** 解析消息的正则表达式。
     *
     * 解析形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    val boRegex = Regex("""${BO_}\s+(?<longIdCode>\d+)\s+(?<msgName>\S+)\s*:\s*(?<length>\S+)\s*(?<node>\S+)?""")
    /** 解析消息行
     *
     * 形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    fun parseBO(rawLine: String): CanMessageImp {
        val line = rawLine.trim()
        require(line.startsWith(BO_)) { "该行不以 '${BO_}' 开头;" }
        val matchGroups  = boRegex.find(line)?.groups ?: error("识别报文的正则表达式识别异常")
        return CanMessageImp().apply {
            val longIdCode : Long = matchGroups["longIdCode"]!!.value.trim().toLong(10)
            msgIdType = CanExternFlag.createByLongIdCode(longIdCode)
            msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
            msgName = matchGroups["msgName"]!!.value.trim().also { it.requireWord() }
            msgLength = matchGroups["length"]!!.value.trim().also { it.requireDecimal() }.toInt()
            nodeName = matchGroups["node"]?.value?.trim()?.also { it.requireWord() } ?: Vector__XXX
        }
    }
    /** 解析节点的正则表达式。
     *
     * 解析形如 BU_: CCS AC
     *
     * */
    val nodeRegex = Regex("""${BU_colon}(?<nodeSet>.*)""")
    /** 解析节点行
     *
     * 形如: BU_: CCS AC
     * */
    fun parseBU(rawLine: String): MutableSet<String> {
        val line = rawLine.trim()
        require(line.startsWith(BU_colon)) { "该行不以 '${BU_colon}' 开头" }
        val matchGroups = nodeRegex.find(line)?.groups ?: error("识别DBC节点的正则表达式识别异常")
        return matchGroups["nodeSet"]?.value?.trim()?.split(" ")?.filter { it.isNotBlank() }?.map { it.trim() }
            ?.also { list -> list.forEach { it.requireWord() } }?.toMutableSet() ?: mutableSetOf()
    }
    /** 解析消息接收节点的行正则表达式。
     *
     * 解析形如: BO_TX_BU_ 2560107544 : Cabin,Test;
     * */
    val msgReceiveNodesRegex = Regex("""$BO_TX_BU_\s+(?<longIdCode>\d+)\s*:\s*(?<nodeSet>.*);""")
    /** 解析消息接收节点行
     *
     * 形如: BO_TX_BU_ 2560107544 : Cabin,Test;
     * */
    fun parseBOTXBU(rawLine: String): Pair<Int, MutableSet<String>> {
        val line = rawLine.trim()
        require(line.startsWith(BO_TX_BU_)) { "该行不以 '${BO_TX_BU_}' 开头" }
        val matchGroups = msgReceiveNodesRegex.find(line)?.groups ?: error("识别报文节点的正则表达式识别异常")
        val longIdCode : Long = matchGroups["longIdCode"]!!.value.trim().toLong(10)
        val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
        val nodeSet = matchGroups["nodeSet"]?.value?.trim()?.split(",")?.filter { it.isNotBlank() }?.map { it.trim() }
            ?.also { list -> list.forEach { it.requireWord() } }?.toMutableSet() ?: mutableSetOf()
        return  msgId to nodeSet
    }
    /** 解析注释的正则表达式。
     *
     * 解析形如
     *
     * CM_ BO_ 2560107544 "报文的注释";
     * */
    val msgCommentRegex = Regex("""$BO_\s+(?<longIdCode>\d+)\s+"(?<comment>[^"]*)"\s*;""")
    /** 解析注释的正则表达式。
     *
     * 解析形如
     *
     * CM_ SG_ 2560107544 CCSToAC1_AirSw "空调开关。";
     * */
    val sigCommentRegex = Regex("""$SG_\s+(?<longIdCode>\d+)\s+(?<sigName>\S+)\s+"(?<comment>[^"]*)"\s*;""")
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
                val matchResult = msgCommentRegex.find(mLine) ?: error("识别报文注释正则表达式识别异常")
                val longIdCode : Long = matchResult.groups["longIdCode"]!!.value.toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val comment = matchResult.groups["comment"]?.value?.trim() ?: ""
                dbc[msgId]?.msgComment = comment
            }
            mLine.startsWith(SG_) -> {
                val matchResult = sigCommentRegex.find(mLine) ?: error("识别信号注释正则表达式识别异常")
                val longIdCode : Long = matchResult.groups["longIdCode"]!!.value.toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val sigName = matchResult.groups["sigName"]!!.value.trim().also { it.requireWord() }
                val comment = matchResult.groups["comment"]?.value?.trim() ?: ""
                dbc[msgId]?.get(sigName)?.signalComment = comment
            }
            // 添加其他注释的解析......
        }
    }
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
    val baDefRegex = Regex("""${BA_DEF_}\s+(?<scope>$BO_|$SG_|$BU_|$EV_|$BU_EV_REL_|$BU_BO_REL_|$BU_SG_REL_)?\s*"(?<name>[^"]+)"\s+(?<valueType>${INT}|${FLOAT}|${STRING}|${ENUM}|${HEX})(?<range>.*);""")
    /** 解析数值类型的自定义属性范围的正则表达式。
     *
     * 例如： BA_DEF_ SG_  "GenSigInactiveValue" INT -5 65535; 中的范围
     * */
    val baDefMinMaxRegex = Regex("""(?<min>\S+)\s+(?<max>\S+)""")
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
        val matchGroups = baDefRegex.find(line)?.groups ?: error("识别自定义属性定义的正则表达式识别异常")
        val valueType : DbcAttributeValueType = DbcAttributeValueType.createBy(matchGroups["valueType"]!!.value.trim())
        // 取最后一个分号之前，到数值类型之后的所有字符，视为属性范围; 再去除空白字符，过滤空白字符。
        val range: String? = matchGroups["range"]?.value?.trim()
        val attribute = DbcAttributeDefinitionImp().apply {
            this.scope = DbcAttributeScopeDefinition.createBy(matchGroups["scope"]?.value?.trim() ?: "")
            this.name = matchGroups["name"]!!.value.trim().also { it.requireWord() }
            this.valueType = valueType
        }
        // 添加属性范围(过滤掉空白字符)
        range?.takeIf { it.isNotBlank() }?.let {
            when (valueType) {
                StringType -> Unit
                // 解析形如： BA_DEF_ BO_  "GenMsgSendType" ENUM  "Cyclic","Event","IfActive","CE","CA";
                Enumeration -> attribute.valueTable = parseRangeToEnumMap(range)
                // 解析形如: BA_DEF_ SG_  "GenSigInactiveValue" INT -5 65535;
                IntegerType, FloatType, HexType -> {
                    val groups = baDefMinMaxRegex.find(range)?.groups ?: error("识别自定义属性定义范围的正则表达式识别异常, 最大值最小值应该由一个空格分开两个值")
                    // 校验数值必须是数值类型，否则报错
                    attribute.min = groups["min"]!!.value.trim().also { it.requireDouble() }
                    attribute.max = groups["max"]!!.value.trim().also { it.requireDouble()
                        require(it.toDouble() >= attribute.min.toDouble()) {"最大值: '${it}' 不能小于最小值"}
                    }
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
    val baDefDefaultRegex = Regex("""${BA_DEF_DEF_}\s+"(?<name>[^"]+)"(?<value>.*);""")
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
        val matchGroups = baDefDefaultRegex.find(line)?.groups ?: error("识别自定义属性默认值的正则表达式识别异常")
        val name = matchGroups["name"]!!.value.trim().also { it.requireWord() }
        val value = matchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性的默认值不可以为空")

        var attribute : DbcAttributeDefinitionImp = dbc.attributeMap[name] ?: error("在DBC中, 找不到属性定义: $name")
        attribute.defaultValue = when (attribute.valueType) {
            // 字符串类型需要去掉 左右 引号，并且去除空白字符
            StringType -> value.trim().also { it.requireStartsAndEnds("\"") } .removeSurrounding("\"").trim()
            // 整形和16进制按照整形处理。需要去除空白字符，并且校验十进制
            IntegerType, HexType -> value.trim().also { it.requireDecimal() }
            FloatType -> value.trim().also { it.requireDouble() }
            // 保存枚举默认值时，直接保存文本值，故这里直接去除双引号即可
            Enumeration -> value.trim().also { it.requireStartsAndEnds("\"") } .removeSurrounding("\"").trim().also {
                require(attribute.valueTable.values.contains(it)) {"自定义属性 '${attribute.name}' 的枚举项不存在: $value"}
            }
        }
    }
    /**解析自定义属性值的正则表达式
     *
     * 解析形如：
     *
     * BA_ "DBName" "Example";
     *
     * BA_ "NmStationAddress" BU_ CCS 2;
     *
     * BA_ "GenMsgCycleTime" BO_ 2560107544 500;
     *
     * BA_ "GenMsgSendType" BO_ 2560107544 1;
     *
     * BA_ "GwUsedMsg" BO_ 2560107544 0;
     *
     * BA_ "GenSigStartValue" SG_ 2434937668 msg2_sig8 0;
     *
     * BA_ "GenSigInactiveValue" SG_ 2434937668 msg1_sig2 100;
     *
     * BA_ "GenSigSendType" SG_ 2434937668 msg1_sig2 2;
     *
     * */
    val baRegex = Regex("""${BA_}\s+"(?<name>[^"]+)"""")
    val baNetRegex = Regex("""${BA_}\s+"(?<name>[^"]+)"\s+(?<value>.*);""")
    val baNodeRegex = Regex("""${BA_}\s+"(?<name>[^"]+)"\s*${BU_}\s+(?<node>\S+)\s+(?<value>.*);""")
    val baMsgRegex = Regex("""${BA_}\s+"(?<name>[^"]+)"\s*${BO_}\s+(?<longIdCode>\d+)\s+(?<value>.*);""")
    val baSigRegex = Regex("""${BA_}\s+"(?<name>[^"]+)"\s*${SG_}\s+(?<longIdCode>\d+)\s+(?<sigName>\S+)(?<value>.*);""")
    /**解析自定义属性值, 并添加到对应的对象中
     *
     * 解析形如：
     *
     * BA_ "DBName" "Example";
     *
     * BA_ "NmStationAddress" BU_ CCS 2;
     *
     * BA_ "GenMsgCycleTime" BO_ 2560107544 500;
     *
     * BA_ "GenMsgSendType" BO_ 2560107544 1;
     *
     * BA_ "GwUsedMsg" BO_ 2560107544 0;
     *
     * BA_ "GenSigStartValue" SG_ 2434937668 msg2_sig8 0;
     *
     * BA_ "GenSigInactiveValue" SG_ 2434937668 msg1_sig2 100;
     *
     * BA_ "GenSigSendType" SG_ 2434937668 msg1_sig2 2;
     *
     * */
    fun parseBaValue(rawLine: String, dbc : DataBaseCanImp) {
        val line = rawLine.trim()
        require(line.startsWith(BA_)) { "该行不以 '${BA_}' 开头" }
        // 优先使用最大的正则表达式，匹配 BA_ 和 自定义属性的名称
        val matchGroups = baRegex.find(line)?.groups ?: error("识别自定义属性值的正则表达式识别异常")
        val name = matchGroups["name"]!!.value.trim().also { it.requireWord() }
        // 通过名字寻找自定义属性
        var attribute = dbc.attributeMap[name] ?: error("在DBC中, 找不到属性定义: $name")
        // 根据作用域来判断属性值, 再分别调用不同的正则表达式
        when (attribute.scope) {
            // 网络类型的作用域，在DBC文件中没有明着写作用域，因此只能通过属性作用域来判断作用域
            DbcAttributeScopeDefinition.Net -> {
                val netMatchGroups = baNetRegex.find(line)?.groups ?: error("识别网络类型自定义属性值时, 正则表达式识别异常")
                val valueText = netMatchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性值不可以为空")
                val attributeData = DbcAttributeData(attribute, DbcAttributeScopeData.Net())
                    .apply { value = parseBaValueByType(valueText, attribute.valueType) }
                // 网络的自定义属性，直接添加到总的属性集合中。
                dbc.attributeValueMap[name] = attributeData
            }
            DbcAttributeScopeDefinition.Node -> {
                val nodeMatchGroups = baNodeRegex.find(line)?.groups ?: error("识别节点类型自定义属性值时, 正则表达式识别异常")
                val nodeName = nodeMatchGroups["node"]!!.value.trim().also { it.requireWord() }
                val valueText = nodeMatchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性值不可以为空")
                DbcAttributeData(attribute, DbcAttributeScopeData.Node(nodeName))
                    .apply { value = parseBaValueByType(valueText, attribute.valueType) }
                // 节点的自定义属性，添加到节点属性集合中; TODO 节点我们没有定义数据结构，这里暂时忽略

            }
            DbcAttributeScopeDefinition.Message -> {
                val msgMatchGroups = baMsgRegex.find(line)?.groups ?: error("识别报文类型自定义属性值时, 正则表达式识别异常")
                val longIdCode = msgMatchGroups["longIdCode"]!!.value.trim().toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val valueText = msgMatchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性值不可以为空")
                val attributeData = DbcAttributeData(attribute, DbcAttributeScopeData.Message(longIdCode))
                    .apply { value = parseBaValueByType(valueText, attribute.valueType) }
                // 报文的自定义属性，添加到报文属性集合中
                dbc[msgId]?.attributeValueMap[name] = attributeData
            }
            DbcAttributeScopeDefinition.Signal -> {
                val sigMatchGroups = baSigRegex.find(line)?.groups ?: error("识别信号类型自定义属性值时, 正则表达式识别异常")
                val longIdCode = sigMatchGroups["longIdCode"]!!.value.trim().toLong(10)
                val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
                val sigName = sigMatchGroups["sigName"]!!.value.trim().also { it.requireWord() }
                val valueText = sigMatchGroups["value"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("自定义属性值不可以为空")
                val attributeData = DbcAttributeData(attribute, DbcAttributeScopeData.Signal(longIdCode, sigName))
                    .apply { value = parseBaValueByType(valueText, attribute.valueType) }
                // 信号的自定义属性，添加到信号属性集合中
                dbc[msgId, sigName]?.attributeValueMap[name] = attributeData
            }
            // 其他类型暂时不支持
            else -> null
        }
    }
    /**解析自定义属性值
     *
     * 根据属性类型，解析属性值
     *
     * */
    fun parseBaValueByType(value: String,  valueType: DbcAttributeValueType ) = when (valueType) {
        // 字符串类型，校验并去除双引号
        StringType -> value.trim().also { it.requireStartsAndEnds("\"") } .removeSurrounding("\"").trim()
        // 数值类型，直接赋值
        IntegerType, FloatType, HexType -> value.trim().also { it.requireDouble() }
        // 枚举类型，保存枚举索引
        Enumeration -> value.trim()
    }
    /** 解析值描述，例如：
     *
     * VAL_ 2560107544 CCSToAC1_AirSw 0 "预留" 1 "关闭" 2 "开启" 3 "无效值未使用" ;
     *
     * VAL_ 2560107544 CCSToCabin1_ColdGearReq 0 "等级零" 1 "等级一" 2 "等级二" 3 "等级三" 4 "等级四" 5 "等级五" 6 "等级六" 7 "等级七" 8 "等级八" 9 "预留" 10 "预留" 11 "预留" 12 "预留" 13 "预留" 14 "预留" 15 "无效值未使用" ;
     *
     * VAL_ 2560104484 CabinToCCS1_FanMotFlt 0 "无故障" 1 "一级故障" 2 "二级故障" 3 "三级故障" ;*/
    val valueTableRegex = Regex("""$VAL_\s+(?<longIdCode>\d+)\s+(?<sigName>\S+)\s+(?<valueTable>.*);""")
    /** 匹配值描述中的键值对 */
    val pairRegex = Regex("""(?<pair>\s*(?<key>\d+)\s*"(?<value>[^"]+)"\s*)""")
    fun parseValueTable (rawLine: String, dbc : DataBaseCanImp) {
        val line = rawLine.trim()
        // 解析出来信号
        val matchGroups = valueTableRegex.find(line)?.groups ?: error("识别值描述的正则表达式识别异常")
        val longIdCode = matchGroups["longIdCode"]!!.value.trim().toLong(10)
        val msgId = CanMessage.transLongIdCodeToMsgId(longIdCode)
        val sigName = matchGroups["sigName"]!!.value.trim().also { it.requireWord() }
        val signal = dbc[msgId]?.get(sigName) ?: error("在DBC中找不到报文: $msgId, 信号: $sigName")

        // 解析值描述
        val valueTableText = matchGroups["valueTable"]?.value?.trim()?.takeIf { it.isNotBlank() } ?: error("值描述不可以为空")
        signal.valueTable = pairRegex.findAll(valueTableText).associateTo(mutableMapOf()) {
            val key = it.groups["key"]!!.value.trim().toInt()
            val value = it.groups["value"]!!.value.trim()
            check(key !in signal.valueTable) {"值描述中存在重复的键: $key"}
            key to value
        }
    }
}