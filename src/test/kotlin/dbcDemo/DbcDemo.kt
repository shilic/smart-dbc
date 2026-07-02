package dbcDemo
import demoData.*
import io.github.shilic.smartDbc.dbc.dataModel.models.CanProtocolImp
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.dataModel.models.DbcBaseInfo
import io.github.shilic.smartDbc.dbc.io.reader.*
import io.github.shilic.smartDbc.dbc.io.writer.*
import io.github.shilic.smartGrid.core.GridReader
import io.github.shilic.smartGrid.utils.fileName
import io.github.shilic.smartGrid.utils.fileNameWithoutExtension
import io.github.shilic.smartGrid.utils.toGsonString
import io.github.shilic.smartGrid.utils.workbook
import java.io.File
import kotlin.test.Test

/**
 * DBC 文件操作测试
 *
 * @author shilic
 */
class DbcDemo {
    /**  测试 DBC 文件读取 */
    @Test
    fun dbcFileReaderTest() {
        println("\n--------------- dbcFileReaderTest 测试开始 -----------------\n")
        // 读取 DBC 文件
        val dbc: DataBaseCanImp = DbcFileReader({ File(ExampleDbcPath3).inputStream() }).read()

        // 你可以在这里对DBC对象做一些编辑

        // 打印DBC对象
        println(dbc.toGsonString())
        println("\n--------------- dbcFileReaderTest 测试结束 -----------------\n")
    }
    /**  测试 DBC 文件写入 */
    @Test
    fun dbcFileWriterTest() {
        println("\n--------------- dbcFileWriterTest 测试开始 -----------------\n")
        // 读取 DBC 文件, 自动处理 GBK 编码和 UTF-8 编码, 避免文件乱码
        val dbc: DataBaseCanImp = DbcFileReader({ File(ExampleDbcPath3).inputStream() }).read()

        // 你可以在这里对DBC对象做一些编辑
        // 例如添加信号，添加报文，添加自定义属性等等。你可以在此基础之上编写界面，来完成DBC文件的编辑。

        // 将DBC对象再次序列化回 .dbc 文件中; 安全写入（自动避免覆盖已有文件）
        val outPath = "output/${ExampleDbcPath3.fileName}"
        DbcFileWriter(dbc).safeWrite(outPath)
        println("\n--------------- dbcFileWriterTest 测试结束 -----------------\n")
    }
    /** 测试从excel表中读取整车协议 */
    @Test
    fun canProtocolsGridReaderTest() {
        println("-------------- 测试整车DBC协议 ------------")
        val canProtocol: CanProtocolImp = DbcGridReader(ExampleExcelPath1).readProtocol().values.first()
        canProtocol.dbcMap.values.forEach { dbc ->
            val outPath = "output/${dbc.dbcTag}.dbc"
            DbcFileWriter(dbc).safeWrite(outPath)
        }
        println("canProtocol: ${canProtocol.toGsonString()}")
    }
    /** 测试从excel表中读取整车协议 */
    @Test
    fun dbcMapGridReaderTest() {
        val dbcMap : MutableMap<String, DataBaseCanImp> = DbcGridReader({ File(ExampleExcelPath1).inputStream()}).read()
        dbcMap.values.forEach { dbc ->
            val outPath = "output/${dbc.dbcTag}.dbc"
            DbcFileWriter(dbc).safeWrite(outPath)
        }
    }
    /** 测试从excel协议表格读取DBC协议 */
    @Test
    fun dbcGridReaderTest() {
        println("\n--------------- dbcGridReaderTest 测试开始 -----------------\n")
        val dbc = DbcGridReader(ExampleExcelPath1.workbook()).read(
            sheetName = "CAN1",
            dbcBaseInfo = DbcBaseInfo(
                dbcTag = "CAN1",
                version = "1.0.0",
                dbcComment = "CAN1",
                baudRate = 500
            )
        )
        println("dbc = ${dbc.toGsonString()}")
        val outPath = "output/${dbc.dbcTag}.dbc"
        DbcFileWriter(dbc).safeWrite(outPath)
        println("\n--------------- dbcGridReaderTest 测试结束 -----------------\n")
    }
}