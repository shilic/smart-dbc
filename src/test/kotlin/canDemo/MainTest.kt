package canDemo

import demoData.*
import demoData.dbcTag1
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import kotlin.test.Test

class MainTest {
    /** 测试程序入口函数 */
    @Test
    fun onCreate() {
        val dbc : DataBaseCanImp = DbcFileReader(ExampleDbcPath1).create().apply {
            dbcTag = dbcTag1
            dbcComment = "DBC描述"
        }

    }
}