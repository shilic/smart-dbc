package dbcDemo

import io.github.shilic.smartDbc.dbc.attributes.enums.DbcAttributeScopeDefinition
import io.github.shilic.smartDbc.dbc.attributes.enums.DbcAttributeValueType
import io.github.shilic.smartDbc.dbc.attributes.models.DbcAttributeDefinitionImp
import io.github.shilic.smartDbc.dbc.dataModel.*
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.*
import io.github.shilic.smartDbc.dbc.dataModel.models.*
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import kotlin.test.Test
import kotlin.test.*

class DbcFileReaderTest {

    // ==================== parseLineStart ====================

    @Test
    fun `parseLineStart with BO_`() {
        assertEquals(BO_, DbcFileReader.parseLineStart("BO_ 2560107544 CCSToAC1: 8 CCS"))
    }

    @Test
    fun `parseLineStart with SG_`() {
        assertEquals(SG_, DbcFileReader.parseLineStart(" SG_ sig1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX"))
    }

    @Test
    fun `parseLineStart with CM_`() {
        assertEquals(CM_, DbcFileReader.parseLineStart("CM_ BO_ 2560107544 \"comment\";"))
    }

    @Test
    fun `parseLineStart with unknown keyword returns null`() {
        assertNull(DbcFileReader.parseLineStart("   UNKNOWN_KEYWORD  some data"))
    }

    // ==================== parseVersion ====================

    @Test
    fun `parseVersion normal`() {
        val result = DbcFileReader.parseVersion("VERSION \"1.0.0\"")
        assertEquals("1.0.0", result)
    }

    @Test
    fun `parseVersion empty`() {
        val result = DbcFileReader.parseVersion("VERSION \"\"")
        assertEquals("", result)
    }

    @Test
    fun `parseVersion wrong prefix throws`() {
        assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseVersion("WRONG \"1.0.0\"")
        }
    }

    // ==================== parseSG ====================

    @Test
    fun `parseSG intel signal`() {
        val sg = DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (1.0,0.0) [0.0|255.0] \"\" Vector__XXX")
        assertEquals("sig1", sg.signalName)
        assertEquals(0, sg.startBit)
        assertEquals(8, sg.bitLength)
        assertEquals(CanByteOrder.Intel, sg.byteOrder)
        assertEquals(CanDataType.Unsigned, sg.dataType)
        assertEquals(1.0, sg.factor)
        assertEquals(0.0, sg.offset)
        assertEquals(0.0, sg.signalMinValuePhys)
        assertEquals(255.0, sg.signalMaxValuePhys)
        assertEquals("", sg.unit)
    }

    @Test
    fun `parseSG motorola signed signal with group`() {
        val sg = DbcFileReader.parseSG(" SG_ motorSig0 m1 : 7|8@0- (1.0,0.0) [0.0|0.0] \"\" Vector__XXX")
        assertEquals("motorSig0", sg.signalName)
        assertEquals(7, sg.startBit)
        assertEquals(8, sg.bitLength)
        assertEquals(CanByteOrder.MotorolaMSB, sg.byteOrder)
        assertEquals(CanDataType.Signed, sg.dataType)
        assertEquals("m1", sg.groupType.dbcValue)
    }

    @Test
    fun `parseSG with unit and receiver nodes`() {
        val sg = DbcFileReader.parseSG(" SG_ sig2 : 0|16@1+ (0.1,-50.0) [-50.0|205.0] \"°C\" Cabin,CCS")
        assertEquals("sig2", sg.signalName)
        assertEquals("°C", sg.unit)
        assertEquals(0.1, sg.factor)
        assertEquals(-50.0, sg.offset)
        assertTrue(sg.sigReceiveNodeSet.containsAll(setOf("Cabin", "CCS")))
    }

    @Test
    fun `parseSG wrong prefix throws`() {
        assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG("BO_ sig1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
    }

    // ==================== parseBO ====================

    @Test
    fun `parseBO extended frame`() {
        val msg = DbcFileReader.parseBO(" BO_ 2560107544 CCSToAC1: 8 CCS")
        assertEquals("CCSToAC1", msg.msgName)
        assertEquals(8, msg.msgLength)
        assertEquals("CCS", msg.nodeName)
        assertEquals(CanExternFlag.Extended, msg.msgIdType)
        assertEquals(0x1898_2418, msg.msgId)
        assertEquals(2560107544L, msg.longIdCode)
    }

    @Test
    fun `parseBO standard frame`() {
        val msg = DbcFileReader.parseBO(" BO_ 1234 StandardMsg: 8 Node1")
        assertEquals("StandardMsg", msg.msgName)
        assertEquals(CanExternFlag.Standard, msg.msgIdType)
        assertEquals(1234, msg.msgId)
    }

    @Test
    fun `parseBO without node defaults to Vector__XXX`() {
        val msg = DbcFileReader.parseBO(" BO_ 2560107544 NoNodeMsg: 8")
        assertEquals("NoNodeMsg", msg.msgName)
        assertEquals(Vector__XXX, msg.nodeName)
    }

    // ==================== parseBU ====================

    @Test
    fun `parseBU normal`() {
        val nodes = DbcFileReader.parseBU(" BU_: CCS AC Cabin")
        assertEquals(setOf("CCS", "AC", "Cabin"), nodes)
    }

    @Test
    fun `parseBU empty node list`() {
        val nodes = DbcFileReader.parseBU("BU_: ")
        assertTrue(nodes.isEmpty())
    }

    // ==================== parseBOTXBU ====================

    @Test
    fun `parseBOTXBU normal`() {
        val (msgId, nodes) = DbcFileReader.parseBOTXBU(" BO_TX_BU_ 2560107544 : Cabin,Test;")
        assertEquals(0x1898_2418, msgId)
        assertEquals(setOf("Cabin", "Test"), nodes)
    }

    @Test
    fun `parseBOTXBU empty node set`() {
        val (msgId, nodes) = DbcFileReader.parseBOTXBU(" BO_TX_BU_ 2560107544 : ;")
        assertEquals(0x1898_2418, msgId)
        assertTrue(nodes.isEmpty())
    }

    // ==================== parseCM ====================

    @Test
    fun `parseCM message comment`() {
        val dbc = DataBaseCanImp().apply {
            set(CanMessageImp().apply { msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "CCSToAC1" })
        }
        DbcFileReader.parseCM(" CM_ BO_ 2560107544 \"空调报文\";", dbc)
        assertEquals("空调报文", dbc[0x1898_2418]?.msgComment)
    }

    @Test
    fun `parseCM signal comment`() {
        val dbc = DataBaseCanImp().apply {
            set(CanMessageImp().apply {
                msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "CCSToAC1"
                set(CanSignalImp().apply { signalName = "CCSToAC1_AirSw" })
            })
        }
        DbcFileReader.parseCM(" CM_ SG_ 2560107544 CCSToAC1_AirSw \"空调开关\";", dbc)
        assertEquals("空调开关", dbc[0x1898_2418]?.get("CCSToAC1_AirSw")?.signalComment)
    }

    // ==================== parseBaDef ====================

    @Test
    fun `parseBaDef INT attribute`() {
        val attr = DbcFileReader.parseBaDef(" BA_DEF_ SG_  \"GenSigStartValue\" INT 0 65535;")
        assertEquals("GenSigStartValue", attr.name)
        assertEquals(DbcAttributeScopeDefinition.Signal, attr.scope)
        assertEquals(DbcAttributeValueType.IntegerType, attr.valueType)
        assertEquals("0", attr.min)
        assertEquals("65535", attr.max)
    }

    @Test
    fun `parseBaDef ENUM attribute`() {
        val attr = DbcFileReader.parseBaDef(" BA_DEF_ BO_  \"GenMsgSendType\" ENUM  \"Cyclic\",\"Event\",\"IfActive\",\"CE\",\"CA\";")
        assertEquals("GenMsgSendType", attr.name)
        assertEquals(DbcAttributeScopeDefinition.Message, attr.scope)
        assertEquals(DbcAttributeValueType.Enumeration, attr.valueType)
        assertEquals("Cyclic", attr.valueTable[0])
        assertEquals("Event", attr.valueTable[1])
    }

    @Test
    fun `parseBaDef STRING attribute no scope`() {
        val attr = DbcFileReader.parseBaDef(" BA_DEF_  \"DBName\" STRING ;")
        assertEquals("DBName", attr.name)
        assertEquals(DbcAttributeScopeDefinition.Net, attr.scope)
        assertEquals(DbcAttributeValueType.StringType, attr.valueType)
    }

    @Test
    fun `parseBaDef FLOAT attribute`() {
        val attr = DbcFileReader.parseBaDef(" BA_DEF_ BO_  \"AttrFloat\" FLOAT -3.5 100.5;")
        assertEquals("AttrFloat", attr.name)
        assertEquals(DbcAttributeValueType.FloatType, attr.valueType)
        assertEquals("-3.5", attr.min)
        assertEquals("100.5", attr.max)
    }

    @Test
    fun `parseBaDef HEX attribute`() {
        val attr = DbcFileReader.parseBaDef(" BA_DEF_ BU_  \"NmStationAddress\" HEX 0 15;")
        assertEquals("NmStationAddress", attr.name)
        assertEquals(DbcAttributeValueType.HexType, attr.valueType)
    }

    // ==================== parseRangeToEnumMap ====================

    @Test
    fun `parseRangeToEnumMap normal`() {
        val map = DbcFileReader.parseRangeToEnumMap(" \"Cyclic\",\"Event\",\"IfActive\",\"CE\",\"CA\"")
        assertEquals(5, map.size)
        assertEquals("Cyclic", map[0])
        assertEquals("Event", map[1])
        assertEquals("CA", map[4])
    }

    // ==================== parseBaDefault ====================

    @Test
    fun `parseBaDefault STRING`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["DBName"] = DbcAttributeDefinitionImp().apply {
                name = "DBName"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.StringType
            }
        }
        DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"DBName\" \"Example\";", dbc)
        assertEquals("Example", dbc.attributeMap["DBName"]?.defaultValue)
    }

    @Test
    fun `parseBaDefault INT`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["GenSigStartValue"] = DbcAttributeDefinitionImp().apply {
                name = "GenSigStartValue"; scope = DbcAttributeScopeDefinition.Signal; valueType = DbcAttributeValueType.IntegerType
            }
        }
        DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"GenSigStartValue\" 0;", dbc)
        assertEquals("0", dbc.attributeMap["GenSigStartValue"]?.defaultValue)
    }

    @Test
    fun `parseBaDefault ENUM`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["GenMsgSendType"] = DbcAttributeDefinitionImp().apply {
                name = "GenMsgSendType"; scope = DbcAttributeScopeDefinition.Message; valueType = DbcAttributeValueType.Enumeration
                valueTable = mutableMapOf(0 to "Cyclic", 1 to "Event")
            }
        }
        DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"GenMsgSendType\" \"Cyclic\";", dbc)
        assertEquals("Cyclic", dbc.attributeMap["GenMsgSendType"]?.defaultValue)
    }

    // ==================== parseBaValueByType ====================

    @Test
    fun `parseBaValueByType STRING`() {
        val result = DbcFileReader.parseBaValueByType(" \"hello\" ", DbcAttributeValueType.StringType)
        assertEquals("hello", result)
    }

    @Test
    fun `parseBaValueByType INT`() {
        val result = DbcFileReader.parseBaValueByType(" 42 ", DbcAttributeValueType.IntegerType)
        assertEquals("42", result)
    }

    @Test
    fun `parseBaValueByType ENUM saves index`() {
        val result = DbcFileReader.parseBaValueByType(" 1", DbcAttributeValueType.Enumeration)
        assertEquals("1", result)
    }

    // ==================== parseBaValue ====================

    @Test
    fun `parseBaValue net scope`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["DBName"] = DbcAttributeDefinitionImp().apply {
                name = "DBName"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.StringType
            }
        }
        DbcFileReader.parseBaValue(" BA_ \"DBName\" \"smartDbc\";", dbc)
        assertEquals("smartDbc", dbc.attributeValueMap["DBName"]?.value)
    }

    @Test
    fun `parseBaValue message scope`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["GenMsgCycleTime"] = DbcAttributeDefinitionImp().apply {
                name = "GenMsgCycleTime"; scope = DbcAttributeScopeDefinition.Message; valueType = DbcAttributeValueType.IntegerType
            }
            set(CanMessageImp().apply { msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "CCSToAC1" })
        }
        DbcFileReader.parseBaValue(" BA_ \"GenMsgCycleTime\" BO_ 2560107544 500;", dbc)
        assertEquals("500", dbc[0x1898_2418]?.attributeValueMap?.get("GenMsgCycleTime")?.value)
    }

    @Test
    fun `parseBaValue signal scope`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["GenSigStartValue"] = DbcAttributeDefinitionImp().apply {
                name = "GenSigStartValue"; scope = DbcAttributeScopeDefinition.Signal; valueType = DbcAttributeValueType.IntegerType
            }
            set(CanMessageImp().apply {
                msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "CCSToAC1"
                set(CanSignalImp().apply { signalName = "CCSToAC1_AirSw" })
            })
        }
        DbcFileReader.parseBaValue(" BA_ \"GenSigStartValue\" SG_ 2560107544 CCSToAC1_AirSw 0;", dbc)
        assertEquals("0", dbc[0x1898_2418, "CCSToAC1_AirSw"]?.attributeValueMap?.get("GenSigStartValue")?.value)
    }

    // ==================== parseValueTable ====================

    @Test
    fun `parseValueTable normal`() {
        val dbc = DataBaseCanImp().apply {
            set(CanMessageImp().apply {
                msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "CCSToAC1"
                set(CanSignalImp().apply { signalName = "CCSToAC1_AirSw" })
            })
        }
        DbcFileReader.parseValueTable(
            " VAL_ 2560107544 CCSToAC1_AirSw 0 \"预留\" 1 \"关闭\" 2 \"开启\" 3 \"无效值未使用\" ;", dbc)
        val vt = dbc[0x1898_2418]?.get("CCSToAC1_AirSw")?.valueTable ?: fail("signal not found")
        assertEquals(4, vt.size)
        assertEquals("预留", vt[0])
        assertEquals("关闭", vt[1])
        assertEquals("开启", vt[2])
        assertEquals("无效值未使用", vt[3])
    }

    // ==================== integration: read() with real DBC files ====================

    @Test
    fun `read Example dbc`() {
        val dbc = DbcFileReader(demoData.ExampleDbcPath1).read()
        assertTrue(dbc.msgMap.isNotEmpty(), "DBC should have messages")
        dbc.msgMap.values.forEach { msg ->
            assertTrue(msg.msgName.isNotBlank(), "message should have a name")
            msg.signalMap.values.forEach { sig ->
                assertTrue(sig.signalName.isNotBlank(), "signal should have a name")
                // longIdCode should be propagated from parent message
                assertEquals(msg.longIdCode, sig.longIdCode,
                    "signal ${sig.signalName} longIdCode should match parent msg ${msg.msgName}")
            }
        }
    }

    @Test
    fun `read GBK encoded dbc`() {
        // GBK DBC 文件包含已知数据问题(155.5 → INT属性), 验证读取器能正确抛出异常而非崩溃
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader(demoData.GBKDbcPath).read()
        }
        assertEquals(ex.message?.contains("155.5"), true)
    }

    @Test
    fun `read UTF8 encoded dbc`() {
        // UTF-8 DBC 文件包含已知数据问题(155.5 → INT属性), 验证读取器能正确抛出异常而非崩溃
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader(demoData.UTF8DbcPath).read()
        }
        assertEquals(ex.message?.contains("155.5"), true)
    }
}
