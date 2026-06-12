package toolTest

import demoData.GBKDbcPath
import demoData.UTF8DbcPath
import io.github.shilic.smartDbc.common.typeExtension.encoding
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.test.Test

class FileToolTest {

    @Test
    fun encodingTest() {
        println()
        val utf8File = File(UTF8DbcPath)
        println("utf8File.encoding : ${utf8File.encoding}")
        val gbkFile = File(GBKDbcPath)
        println("gbkFile.encoding : ${gbkFile.encoding}")
    }
    @Test
    fun demonstrateMultipleClose() {
        println("测试 InputStream 多次关闭:")

        val file = File.createTempFile("test", ".txt")
        file.writeText("test data")

        val inputStream = FileInputStream(file)

        // 第一次关闭
        println("第一次关闭...")
        inputStream.close()
        println("  成功关闭")

        // 尝试读取（应该失败）
        try {
            inputStream.read()
        } catch (e: IOException) {
            println("  读取失败: ${e.message}")
        }

        // 第二次关闭
        println("第二次关闭...")
        try {
            inputStream.close()  // 通常不会抛出异常
            println("  成功关闭（但实际没做任何事）")
        } catch (e: Exception) {
            println("  异常: ${e.message}")
        }

        file.delete()
    }
}