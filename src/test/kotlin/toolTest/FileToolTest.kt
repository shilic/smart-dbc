package toolTest

import io.github.shilic.smartDbc.common.typeExtension.encoding
import java.io.File
import kotlin.test.Test

class FileToolTest {

    @Test
    fun encodingTest() {
        println()
        val utf8File = File("src/test/resources/DBC/大屏协议（测试版2）UTF-8编码.dbc")
        println("utf8File.encoding : ${utf8File.encoding}")
        val gbkFile = File("src/test/resources/DBC/大屏协议（测试版2）GBK编码.dbc")
        println("gbkFile.encoding : ${gbkFile.encoding}")
    }
}