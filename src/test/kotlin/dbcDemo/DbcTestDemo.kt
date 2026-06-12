package dbcDemo
import demoData.ExampleDbcPath3
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import java.io.File
import kotlin.test.Test

class DbcTestDemo {
    @Test
    fun test1() {
        println("\n--------------- 开始测试 -----------------\n")
        val dbc: DataBaseCanImp = DbcFileReader(File(ExampleDbcPath3).inputStream()).create()
        println(dbc.toString())
        println("\n--------------- 结束测试 -----------------\n")
    }

}