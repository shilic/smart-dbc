package core

import java.io.InputStream

/** DBC输入接口，兼容不同平台的文件输入方式 */
fun interface DbcInputInterface {
    fun getInputStream(dbcFilePath: String): InputStream
}
