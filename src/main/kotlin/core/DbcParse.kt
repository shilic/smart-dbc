package core

import dataModel.models.CanDbc
import dataModel.models.CanMessage
import dataModel.models.CanSignal
import dataModel.dataEnums.CANByteOrder
import dataModel.dataEnums.CANDataType
import dataModel.dataEnums.CanMsgIdType
import dataModel.dataEnums.MatrixGroupType
import tool.toHexStr
import java.io.*
import java.nio.charset.Charset

/**
 * DBC文件解析器。将DBC文件解析为DBC对象。暂不支持报文分组。
 */
object DbcParse {

    private const val Vector__XXX = "Vector__XXX"

    private val sgRegex = Regex(
        """SG_\s*(?<sigName>\b[a-zA-Z_]\w*)\s*(?<group>[mM]\d*)?\s*:""" +
        """\s*(?<startBit>\d+)\s*[|]\s*(?<bitLength>\d+)@(?<ByteOrder>[10])(?<DataType>[+-])""" +
        """\s*\((?<Factor>-?\b\d[\d.]*),(?<Offset>-?\b\d[\d.]*)\)\s*\[(?<min>-?\b\d[\d.]*)\|(?<max>-?\b\d[\d.]*)\]\s*""" +
        """"(?<unit>[^"]*)"\s*(?<nodeSet>[\w,]+)"""
    )
    private val boRegex = Regex(
        """BO_\s*(?<longIdCode>\d+)\s*(?<msgName>\b[a-zA-Z_]\w*)\s*:\s*(?<length>\d)\s*(?<node>\b[a-zA-Z_]\w*)"""
    )
    private val titleRegex = Regex("""^(?<title>VERSION|BU_:|BO_|SG_|BO_TX_BU_|CM_|BA_DEF_|BA_DEF_DEF_|BA_|VAL_)\s+""")
    private val nodeRegex = Regex("""(\s+(?<node>[a-zA-Z_]*))""")
    private val groupFlagRegex = Regex("""(?<M>M)|(m(?<num>\d+))""")
    private val receiveNodeRegex = Regex("""(?<node>\b[a-zA-Z_]\w*)""")

    /** 从 InputStream 解析 DBC */
    fun getDbcFromInputStream(dbcTag: String, inputStream: InputStream): CanDbc {
        val dbc = CanDbc()
        try {
            inputStream.reader(Charset.forName("GBK")).buffered().use { parseLines(dbc, it) }
        } catch (e: IOException) { error("DbcParse: IO异常") }
        return dbc
    }

    /** 从文件路径解析 DBC */
    fun getDbcFromFilePath(dbcTag: String, filePath: String): CanDbc {
        val file = File(filePath)
        require(file.exists()) { "DbcParse：文件不存在" }
        require(!file.isDirectory) { "DbcParse：该文件是目录" }
        require(file.name.endsWith(".dbc")) { "DbcParse：不是DBC文件" }
        val dbc = CanDbc()
        try {
            file.reader(Charset.forName("GBK")).buffered().use { parseLines(dbc, it) }
        } catch (e: IOException) { error("DbcParse：IO异常") }
        return dbc
    }

    /** 逐行解析 */
    private fun parseLines(dbc: CanDbc, reader: BufferedReader) {
        reader.forEachLine { line ->
            val trimmed = line.trim()
            val match = titleRegex.find(trimmed) ?: return@forEachLine
            when (match.groupValues[1]) {
                "VERSION" -> println("DbcParse：版本信息：$trimmed")
                "BU_:" -> dbc.nodeSet.addAll(parseBU(trimmed))
                "BO_" -> {
                    val msg = parseBO(trimmed)
                    dbc.msgMap.putIfAbsent(msg.msgId.toHexStr(), msg)
                }
                "SG_" -> {
                    val sig = parseSG(trimmed)
                    dbc.getMsgAt(dbc.msgMap.size - 1)?.signalMap?.putIfAbsent(sig.signalName, sig)
                }
            }
        }
    }

    /** 解析信号行 */
    fun parseSG(line: String): CanSignal {
        val trimmed = line.trim()
        require(trimmed.startsWith("SG_")) { "DbcParse：该行不是信号: $trimmed" }
        val m = sgRegex.find(trimmed) ?: error("DbcParse：识别异常: $trimmed")
        val g = m.groups

        var groupNum = -1
        var groupType : MatrixGroupType = MatrixGroupType.DefaultGroup
        val groupStr = g["group"]?.value
        if (groupStr != null) {
            val gm = groupFlagRegex.find(groupStr)
            if (gm != null) {
                when {
                    gm.groups["M"] != null -> MatrixGroupType.GroupFlag
                    gm.groups["num"] != null -> {
                        groupNum = gm.groupValues[2].toInt()
                        groupType = MatrixGroupType.CustomGroup(groupNum)

                    }
                }
            }
        }
        val byteOrder = if (g["ByteOrder"]!!.value == "0") CANByteOrder.MotorolaMSB else CANByteOrder.Intel
        val dataType = if (g["DataType"]!!.value == "-") CANDataType.Signed else CANDataType.Unsigned
        val unit = g["unit"]?.value ?: ""

        val receiveNodeSet = hashSetOf<String>()
        val nodeStr = g["nodeSet"]?.value
        if (nodeStr != null) {
            receiveNodeRegex.findAll(nodeStr).forEach { it.groups["node"]?.value?.let { n -> receiveNodeSet.add(n) } }
        } else { receiveNodeSet.add(Vector__XXX) }

        return CanSignal()
    }

    /** 解析消息行 */
    fun parseBO(line: String): CanMessage {
        val trimmed = line.trim()
        require(trimmed.startsWith("BO_")) { "DbcParse：该行不是CAN消息: $trimmed" }
        val m = boRegex.find(trimmed) ?: error("DbcParse：识别异常: $trimmed")
        val g = m.groups
        val longIdCode = g["longIdCode"]!!.value.toLong(10)
        val msgIdType = if (longIdCode > 0x7FF) CanMsgIdType.Extended else CanMsgIdType.Standard
        val msgId = if (msgIdType == CanMsgIdType.Extended) CanMessage.transIdCodeToID(longIdCode) else longIdCode.toInt()
        return CanMessage()
    }

    /** 解析节点行 */
    fun parseBU(line: String): Set<String> {
        val trimmed = line.trim()
        require(trimmed.startsWith("BU_:")) { "DbcParse：该行不是节点: $trimmed" }
        return nodeRegex.findAll(trimmed).mapNotNull { it.groups["node"]?.value }.toSet()
    }
}
