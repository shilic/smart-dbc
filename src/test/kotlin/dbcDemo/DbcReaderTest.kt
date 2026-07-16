package dbcDemo

import demoData.ExampleDbcPath1
import demoData.ExampleExcelPath1
import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.dataModel.models.DbcBaseInfo
import io.github.shilic.smartDbc.dbc.io.reader.*
import io.github.shilic.smartGrid.utils.fileName
import io.github.shilic.smartGrid.utils.workbook
import java.io.File
import kotlin.test.Test
import kotlin.test.*

/**
 * DbcReader 扩展函数测试 —— 验证 toDbc() / getDbc() / getDbcMap() 各类重载
 */
class DbcReaderTest {

    // ==================== toDbc ====================

    @Test
    fun `String toDbc from dbc file`() {
        val dbc: DataBaseCanImp = ExampleDbcPath1.toDbc()
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty(), "should have messages")
    }

    @Test
    fun `File toDbc from dbc file`() {
        val dbc: DataBaseCanImp = File(ExampleDbcPath1).toDbc()
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    @Test
    fun `File toDbc throws on non-dbc extension`() {
        val tmp = File.createTempFile("test", ".txt").also { it.writeText("hello") }
        try {
            val ex = assertFailsWith<IllegalStateException> { tmp.toDbc() }
            assertTrue(ex.message!!.contains("只支持直接将dbc后缀的文件转换为"))
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `lambda toDbc from input stream provider`() {
        val dbc: DataBaseCanImp = File(ExampleDbcPath1)::inputStream.toDbc()
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    // ==================== getDbc ====================

    @Test
    fun `File getDbc from Excel sheet CAN1`() {
        val dbc: DataBaseCanImp = File(ExampleExcelPath1).getDbc("CAN1")
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty(), "CAN1 should have messages")
    }

    @Test
    fun `File getDbc with dbcBaseInfo`() {
        val dbc = File(ExampleExcelPath1).getDbc("CAN1", DbcBaseInfo(
            dbcTag = "CAN1_Test", version = "2.0", dbcComment = "test", baudRate = 250
        ))
        assertEquals("CAN1_Test", dbc.dbcTag)
        assertEquals("2.0", dbc.version)
        assertEquals("test", dbc.dbcComment)
        assertEquals(250, dbc.baudRate)
    }

    @Test
    fun `String getDbc from Excel sheet`() {
        val dbc: DataBaseCanImp = ExampleExcelPath1.getDbc("CAN1")
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    @Test
    fun `Workbook getDbc from Excel sheet`() {
        val dbc: DataBaseCanImp = ExampleExcelPath1.workbook().getDbc("CAN1")
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    @Test
    fun `InputStream getDbc from Excel file`() {
        val dbc: DataBaseCanImp =
            File(ExampleExcelPath1).inputStream().use { it.getDbc("CAN1") }
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    @Test
    fun `lambda getDbc from input stream provider`() {
        val dbc: DataBaseCanImp = File(ExampleExcelPath1)::inputStream.getDbc("CAN1")
        assertNotNull(dbc)
        assertTrue(dbc.msgMap.isNotEmpty())
    }

    // ==================== getDbcMap ====================

    @Test
    fun `File getDbcMap from Excel`() {
        val map: MutableMap<String, DataBaseCanImp> = File(ExampleExcelPath1).getDbcMap()
        assertTrue(map.isNotEmpty(), "should have at least one DBC")
        map.values.forEach { dbc ->
            assertTrue(dbc.dbcTag.isNotBlank(), "every DBC should have a tag")
            assertTrue(dbc.msgMap.isNotEmpty(), "every DBC should have messages")
        }
    }

    @Test
    fun `String getDbcMap from Excel path`() {
        val map: MutableMap<String, DataBaseCanImp> = ExampleExcelPath1.getDbcMap()
        assertTrue(map.isNotEmpty())
        map.values.forEach { dbc ->
            // verify longIdCode propagated to signals
            dbc.msgMap.values.forEach { msg ->
                msg.signalMap.values.forEach { sig ->
                    assertEquals(msg.longIdCode, sig.longIdCode,
                        "signal ${sig.signalName} longIdCode should match parent")
                }
            }
        }
    }

    @Test
    fun `Workbook getDbcMap from Excel`() {
        val map: MutableMap<String, DataBaseCanImp> = ExampleExcelPath1.workbook().getDbcMap()
        assertTrue(map.isNotEmpty())
    }

    @Test
    fun `InputStream getDbcMap from Excel`() {
        val map: MutableMap<String, DataBaseCanImp> =
            File(ExampleExcelPath1).inputStream().use { it.getDbcMap() }
        assertTrue(map.isNotEmpty())
    }

    @Test
    fun `lambda getDbcMap from input stream provider`() {
        val map: MutableMap<String, DataBaseCanImp> =
            File(ExampleExcelPath1)::inputStream.getDbcMap()
        assertTrue(map.isNotEmpty())
    }

    // Note: Excel → DBC → re-read round-trip skipped due to known DBC reader limitation
    // with wrapped/multi-line CM_ SG_ comment lines. The reader requires each DBC statement
    // on a single line, but the writer may produce long comment lines that span multiple lines.
}
