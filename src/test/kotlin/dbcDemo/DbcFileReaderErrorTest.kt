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

/**
 * DbcFileReader 输入校验错误测试 —— 验证所有 requireXxx() 校验在非法输入下正确抛出异常
 */
class DbcFileReaderErrorTest {

    // ==================== parseLineStart ====================
    // 不会抛异常, 跳过

    // ==================== parseVersion ====================

    @Test
    fun `parseVersion throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> { DbcFileReader.parseVersion("WRONG \"1.0\"") }
        assertTrue(ex.message!!.contains("不以 'VERSION' 开头"))
    }

    // ==================== parseSG ====================

    @Test
    fun `parseSG throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG("BO_ sig1 : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不以 'SG_' 开头"))
    }

    @Test
    fun `parseSG throws on non-word signalName`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ 123badName : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseSG throws on signalName with special chars`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig-name : 0|8@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseSG throws on non-decimal startBit`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : abc|8@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个十进制整数"))
    }

    @Test
    fun `parseSG throws on non-decimal bitLength`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|xyz@1+ (1,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个十进制整数"))
    }

    @Test
    fun `parseSG throws on non-double factor`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (abc,0) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseSG throws on non-double offset`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (1,abc) [0|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseSG throws on non-double minPhys`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (1,0) [abc|255] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseSG throws on non-double maxPhys`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (1,0) [0|abc] \"\" Vector__XXX")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseSG throws on non-word receiver node`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1+ (1,0) [0|255] \"\" 123bad")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseSG throws on invalid byteOrder`() {
        // byteOrder must be 0 or 1; '2' is invalid
        assertFailsWith<IllegalStateException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@2+ (1,0) [0|255] \"\" Vector__XXX")
        }
    }

    @Test
    fun `parseSG throws on invalid dataType`() {
        // dataType must be + or -; '*' is invalid
        assertFailsWith<IllegalStateException> {
            DbcFileReader.parseSG(" SG_ sig1 : 0|8@1* (1,0) [0|255] \"\" Vector__XXX")
        }
    }

    // ==================== parseBO ====================

    @Test
    fun `parseBO throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBO(" SG_ 2560107544 CCSToAC1: 8 CCS")
        }
        assertTrue(ex.message!!.contains("不以 'BO_' 开头"))
    }

    @Test
    fun `parseBO throws on non-word msgName`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBO(" BO_ 2560107544 123badName: 8 CCS")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseBO throws on non-decimal msgLength`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBO(" BO_ 2560107544 Msg1: abc CCS")
        }
        assertTrue(ex.message!!.contains("不是一个十进制整数"))
    }

    @Test
    fun `parseBO throws on non-word nodeName`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBO(" BO_ 2560107544 Msg1: 8 123BadNode")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    // ==================== parseBU ====================

    @Test
    fun `parseBU throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBU(" BO_ CCS AC")
        }
        assertTrue(ex.message!!.contains("不以 'BU_:' 开头"))
    }

    @Test
    fun `parseBU throws on non-word node`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBU(" BU_: 123bad")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    // ==================== parseBOTXBU ====================

    @Test
    fun `parseBOTXBU throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBOTXBU(" BO_ 2560107544 : Cabin;")
        }
        assertTrue(ex.message!!.contains("不以 'BO_TX_BU_' 开头"))
    }

    @Test
    fun `parseBOTXBU throws on non-word receiver node`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBOTXBU(" BO_TX_BU_ 2560107544 : 123bad;")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    // ==================== parseCM ====================

    @Test
    fun `parseCM throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseCM(" BA_ BO_ 2560107544 \"comment\";", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("不以 'CM_' 开头"))
    }

    // ==================== parseBaDef ====================

    @Test
    fun `parseBaDef throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDef(" BA_ SG_ \"test\" INT 0 100;")
        }
        assertTrue(ex.message!!.contains("不以 'BA_DEF_' 开头"))
    }

    @Test
    fun `parseBaDef throws on non-word name`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDef(" BA_DEF_ SG_  \"123bad\" INT 0 100;")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseBaDef throws when max less than min`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDef(" BA_DEF_ SG_  \"Test\" INT 100 0;")
        }
        assertTrue(ex.message!!.contains("不能小于最小值"))
    }

    // ==================== parseRangeToEnumMap ====================

    @Test
    fun `parseRangeToEnumMap throws on missing quotes`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseRangeToEnumMap(" Cyclic,Event")
        }
        assertTrue(ex.message!!.contains("不是以"))
    }

    @Test
    fun `parseRangeToEnumMap throws on duplicate entries`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseRangeToEnumMap(" \"A\",\"A\"")
        }
        assertTrue(ex.message!!.contains("不能重复"))
    }

    @Test
    fun `parseRangeToEnumMap throws on blank entry`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseRangeToEnumMap(" \"A\",\"   \"")
        }
        assertTrue(ex.message!!.contains("不能为空白字符"))
    }

    @Test
    fun `parseRangeToEnumMap throws on non-word entry`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseRangeToEnumMap(" \"abc\",\"123\"")
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    // ==================== parseBaDefault ====================

    @Test
    fun `parseBaDefault throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDefault(" BA_ \"test\" 0;", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("不以 'BA_DEF_DEF_' 开头"))
    }

    @Test
    fun `parseBaDefault throws when attribute not found`() {
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"NotFound\" 0;", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("找不到属性定义"))
    }

    @Test
    fun `parseBaDefault throws on bad STRING value missing quotes`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["DBName"] = DbcAttributeDefinitionImp().apply {
                name = "DBName"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.StringType
            }
        }
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"DBName\" noquotes;", dbc)
        }
        assertTrue(ex.message!!.contains("不是以"))
    }

    @Test
    fun `parseBaDefault throws on bad INT value`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["Test"] = DbcAttributeDefinitionImp().apply {
                name = "Test"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.IntegerType
            }
        }
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"Test\" abc;", dbc)
        }
        assertTrue(ex.message!!.contains("不是一个十进制整数"))
    }

    @Test
    fun `parseBaDefault throws on bad FLOAT value`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["Test"] = DbcAttributeDefinitionImp().apply {
                name = "Test"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.FloatType
            }
        }
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"Test\" abc;", dbc)
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseBaDefault throws on ENUM value not in table`() {
        val dbc = DataBaseCanImp().apply {
            attributeMap["Test"] = DbcAttributeDefinitionImp().apply {
                name = "Test"; scope = DbcAttributeScopeDefinition.Net; valueType = DbcAttributeValueType.Enumeration
                valueTable = mutableMapOf(0 to "A", 1 to "B")
            }
        }
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDefault(" BA_DEF_DEF_  \"Test\" \"C\";", dbc)
        }
        assertTrue(ex.message!!.contains("枚举项不存在"))
    }

    // ==================== parseBaValueByType ====================

    @Test
    fun `parseBaValueByType STRING throws on no quotes`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaValueByType(" noquotes ", DbcAttributeValueType.StringType)
        }
        assertTrue(ex.message!!.contains("不是以"))
    }

    // ==================== parseBaValue ====================

    @Test
    fun `parseBaValue throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaValue(" CM_ \"test\" \"val\";", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("不以 'BA_' 开头"))
    }

    @Test
    fun `parseBaValue throws when attribute not found`() {
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader.parseBaValue(" BA_ \"NotFound\" \"val\";", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("找不到属性定义"))
    }

    // ==================== parseValueTable ====================

    @Test
    fun `parseValueTable throws on wrong prefix`() {
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader.parseValueTable(" CM_ 2560107544 sig1 0 \"a\";", DataBaseCanImp())
        }
        assertTrue(ex.message!!.contains("识别值描述的正则表达式识别异常"))
    }

    @Test
    fun `parseValueTable throws on non-word sigName`() {
        val dbc = DataBaseCanImp().apply {
            set(CanMessageImp().apply { msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "M" })
        }
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseValueTable(" VAL_ 2560107544 123bad 0 \"a\";", dbc)
        }
        assertTrue(ex.message!!.contains("不是 word类型"))
    }

    @Test
    fun `parseValueTable throws on duplicate key`() {
        val dbc = DataBaseCanImp().apply {
            set(CanMessageImp().apply {
                msgId = 0x1898_2418; msgIdType = CanExternFlag.Extended; msgName = "M"
                set(CanSignalImp().apply { signalName = "sig"; valueTable[0] = "old" })
            })
        }
        val ex = assertFailsWith<IllegalStateException> {
            DbcFileReader.parseValueTable(" VAL_ 2560107544 sig 0 \"newVal\" ;", dbc)
        }
        assertTrue(ex.message!!.contains("重复的键"))
    }

    // ==================== parseBaDef min/max validation ====================

    @Test
    fun `parseBaDef HEX throws on non-double min`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDef(" BA_DEF_ SG_ \"Test\" HEX abc 100;")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }

    @Test
    fun `parseBaDef HEX throws on non-double max`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DbcFileReader.parseBaDef(" BA_DEF_ SG_ \"Test\" HEX 0 abc;")
        }
        assertTrue(ex.message!!.contains("不是一个有效的十进制数"))
    }
}
