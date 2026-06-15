package dbcDemo
import demoData.ExampleDbcPath3
import io.github.shilic.smartDbc.common.typeExtension.*
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
 * @date 2022/11/07
 */
class DbcDemo {
    /**
     * 测试 DBC 文件读取
     */
    @Test
    fun dbcFileReaderTest() {
        println("\n--------------- 开始测试 -----------------\n")
        val dbc: DataBaseCanImp = DbcFileReader(File(ExampleDbcPath3).inputStream()).create()
        println(dbc.toGsonString())
        println("\n--------------- 结束测试 -----------------\n")
    }
    /**
     * 测试 DBC 文件写入
     */
    @Test
    fun sequenceTest() {
        println("\n--------------- 开始测试 -----------------\n")
        val dbc: DataBaseCanImp = DbcFileReader(File(ExampleDbcPath3).inputStream()).create()
        DbcFileWriter(dbc).writeTo(ExampleDbcPath3.nextAvailablePath())
        println("\n--------------- 结束测试 -----------------\n")
    }
}