package io.github.shilic.smartDbc.dbc.io.reader

import io.github.shilic.smartDbc.dbc.dataModel.DEFAULT_NODE
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.models.*

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/**
 * DBC文件解析器。通过.dbc后缀的文件, 解析为DBC对象。
 */
class DbcFileReader {
    // --------------------- 文件基本信息 ----------------------
    /** 传入文件流 */
    private val inputStream: InputStream
    /** 是否需要关闭文件 */
    val closeFile: Boolean

    // +++++++++++++++++++++ 可选参数 +++++++++++++++++++++++++
    /** 打开文件时的编码；需要自动兼容 UTF-8 和 GBK（GB2312）；默认是GBK */
    var charset: Charset = Charset.forName("GBK")

    // ---------------- java风格构造函数, 用于精确控制构造行为 ----------------
    /** 直接通过文件输入流的方式初始化一个 'DBC文件解析器'
     *
     * 此时不需要自动关闭文件输入流，由外部程序自行关闭文件。
     * */
    @Suppress("UNUSED")
    constructor(inputStream: InputStream) {
        this.inputStream = inputStream
        this.closeFile = false
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
    }

    // --------------------- 正则表达式们 ----------------------
    /** 匹配标题的正则表达式。*/
    private val titleRegex = Regex("""^(?<title>VERSION|BS_:|BU_:|BO_|SG_|BO_TX_BU_|CM_|BA_DEF_|BA_DEF_DEF_|BA_|VAL_)\s+""")
    private val versionRegex = Regex("""^VERSION\s+"(?<version>[^"]*)"""")
    /** 解析信号的正则表达式。
     *
     * 解析形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS  */
    private val sgRegex = Regex(
        """SG_\s+(?<sigName>[a-zA-Z_]\w*)\s*(?<group>[mM]\d*)?\s*:""" +
        """\s+(?<startBit>\d+)\s*[|]\s*(?<bitLength>\d+)\s*@\s*(?<ByteOrder>[10])\s*(?<DataType>[+-])""" +
        """\s*\(\s*(?<Factor>-?\d[\d.]*)\s*,\s*(?<Offset>-?\d[\d.]*)\s*\)\s*\[\s*(?<min>-?\d[\d.]*)\s*[|]\s*(?<max>-?\d[\d.]*)]\s*""" +
        """"(?<unit>[^"]*)"\s*(?<nodeSet>[\w,]*)?"""
    )
    /** 解析消息的正则表达式。
     *
     * 解析形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    private val boRegex = Regex("""BO_\s+(?<longIdCode>\d+)\s+(?<msgName>[a-zA-Z_]\w*)\s*:\s*(?<length>\d+)\s*(?<node>[a-zA-Z_]\w*)?""")
    /** 解析节点的正则表达式。
     *
     * 解析形如 BU_: CCS AC
     *
     * */
    private val nodeRegex = Regex("""BU_:(?<nodes>[\w\s]*)?""")

    /** 主函数: 解析 DBC;
     *
     * 并自动判断是否应该关闭文件 */
    fun createMutableDbc(): DataBaseCanImp  = parseLines(inputStream.reader(charset).buffered()).also { if (closeFile) { inputStream.close() } }

    /** 逐行解析 */
    private fun parseLines(reader: BufferedReader) : DataBaseCanImp  {
        val dbc = DataBaseCanImp()
        // 行号，调使用
        var lineNumber = 0
        // 使用forEachLine自动处理资源关闭
        reader.useLines { lines ->
            lines.forEach { rawLine ->
                lineNumber += 1
                val line = rawLine.trim()
                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                    return@forEach
                }
                // 跳过匹配失败的行
                val match = titleRegex.find(line) ?: return@forEach
                when (match.groups["title"]!!.value) {
                    "VERSION" -> dbc.version = parseVersion(lineNumber, line)
                    "BU_:" -> dbc.nodeSet.addAll(parseBU(lineNumber, line))
                    "BO_" -> {
                        val msg = parseBO(lineNumber, line)
                        dbc.msgMap.putIfAbsent(msg.dbcKey, msg)
                    }
                    "SG_" -> {
                        val sig = parseSG(lineNumber, line)
                        dbc.getMsgAt(dbc.msgMap.size - 1)?.signalMap?.putIfAbsent(sig.dbcKey, sig)
                    }
                }
            }
        }
        return dbc
    }
    /** 解析版本行
     *
     * 形如 VERSION "DBC_VERSION"
     * */
    fun parseVersion(lineNumber: Int, rawLine: String): String {
        val line = rawLine.trim()
        require(line.startsWith("VERSION")) { "${DbcFileReader::class.simpleName}：该行不以 'VERSION' 开头; 错误行号: $lineNumber, 行内容: $line" }
        val matchResult = versionRegex.find(line) ?: error("${DbcFileReader::class.simpleName}：正则表达式识别异常; 错误行号: $lineNumber, 行内容: $line")
        return matchResult.groups["version"]?.value ?: ""
    }

    /** 解析信号行
     *
     * 形如 SG_ intel1 m1 : 24|8@1+ (1,0) [0|255] ""  Cabin,CCS
     * */
    fun parseSG(lineNumber: Int, rawLine: String): CanSignalImp {
        val line = rawLine.trim()
        require(line.startsWith("SG_")) { "${DbcFileReader::class.simpleName}：该行不以 'SG_' 开头; 错误行号: $lineNumber, 行内容: $line" }
        val matchResult = sgRegex.find(line) ?: error("${DbcFileReader::class.simpleName}：正则表达式识别异常; 错误行号: $lineNumber, 行内容: $line")

        val matchGroups = matchResult.groups
        val sig = CanSignalImp()

        sig.signalName = matchGroups["sigName"]!!.value
        matchGroups["group"]?.value?.let { sig.groupType = MatrixGroupType.createBy(it) }

        sig.startBit = matchGroups["startBit"]!!.value.toInt()
        sig.bitLength = matchGroups["bitLength"]!!.value.toInt()
        sig.byteOrder = if (matchGroups["ByteOrder"]!!.value == "0") CanByteOrder.MotorolaMSB else CanByteOrder.Intel
        sig.dataType = if (matchGroups["DataType"]!!.value == "-") CanDataType.Signed else CanDataType.Unsigned

        sig.factor = matchGroups["Factor"]!!.value.toDouble()
        sig.offset = matchGroups["Offset"]!!.value.toDouble()
        sig.signalMinValuePhys = matchGroups["min"]!!.value.toDouble()
        sig.signalMaxValuePhys = matchGroups["max"]!!.value.toDouble()
        sig.unit = matchGroups["unit"]?.value ?: ""

        matchGroups["nodeSet"]?.value?.split(",")?.let { sig.sigReceiveNodeSet.addAll(it) }
        return sig
    }

    /** 解析消息行
     *
     * 形如 BO_ 2551255586 GroupTest: 8 Test
     * */
    fun parseBO(lineNumber: Int, rawLine: String): CanMessageImp {
        val line = rawLine.trim()
        require(line.startsWith("BO_")) { "${DbcFileReader::class.simpleName}：该行不以 'BO_' 开头; 错误行号: $lineNumber, 行内容: $line" }
        val matchResult  = boRegex.find(line) ?: error("${DbcFileReader::class.simpleName}：正则表达式识别异常; 错误行号: $lineNumber, 行内容: $line")

        val matchGroups = matchResult.groups
        val msg = CanMessageImp()

        val longIdCode : Long = matchGroups["longIdCode"]!!.value.toLong(10)
        msg.msgIdType = if (longIdCode > 0x8000_0000) CanExternFlag.Extended else CanExternFlag.Standard
        msg.msgId = CanMessage.transIdCodeToID(longIdCode, msg.msgIdType)
        msg.msgName = matchGroups["msgName"]!!.value
        msg.msgLength = matchGroups["length"]!!.value.toInt()
        msg.nodeName = matchGroups["node"]?.value ?: DEFAULT_NODE
        return msg
    }

    /** 解析节点行 */
    fun parseBU(lineNumber: Int, rawLine: String): Set<String> {
        val line = rawLine.trim()
        require(line.startsWith("BU_:")) { "${DbcFileReader::class.simpleName}：该行不以 'BU_:' 开头; 错误行号: $lineNumber, 行内容: $line" }
        return nodeRegex.find(line)?.groups["nodes"]?.value?.split(" ")?.toSet() ?: emptySet()
    }
}