package dbcDemo
import demoData.ExampleDbcPath3
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import io.github.shilic.smartDbc.dbc.io.writer.DbcFileWriter
import io.github.shilic.smartGrid.utils.toGsonString
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
        // 读取 DBC 文件
        val dbc: DataBaseCanImp = DbcFileReader({ File(ExampleDbcPath3).inputStream() }).read()

        // 你可以在这里对DBC对象做一些编辑

        // 将DBC对象再次序列化回文件 中
        DbcFileWriter(dbc).safeWrite(ExampleDbcPath3)
        println("\n--------------- dbcFileWriterTest 测试结束 -----------------\n")
    }
}