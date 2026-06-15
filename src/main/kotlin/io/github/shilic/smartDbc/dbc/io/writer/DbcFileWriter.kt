package io.github.shilic.smartDbc.dbc.io.writer

import io.github.shilic.smartDbc.common.typeExtension.encoding
import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**  DBC文件写入器 */
class DbcFileWriter (
    val dbc: DataBaseCan,
    /** 初始化参数：默认编码为GBK */
    val defaultEncoding: Charset = Charset.forName("GBK")
) {
    /**  获取DBC文件的所有行 */
    val sequence: Sequence<String> get() = dbc.allSequence
    fun writeTo(path: String) = writeTo(File(path))
    fun writeTo(file: File) {
        // 决定使用的编码
        val charset = if (file.exists()) {
            // 文件存在：尝试读取原有编码，失败则回退到默认编码
            file.encoding ?: defaultEncoding
        } else {
            // 文件不存在：使用默认编码（GBK）
            defaultEncoding
        }
        // 惰性消费序列，逐行写入
        file.bufferedWriter(charset).use { writer ->
            sequence.forEach { line ->
                // writer.write(line)写入当前行的文本内容（不含换行符）
                writer.write(line)
                // writer.newLine()写入系统相关的换行符（Windows 是 \r\n，Unix/Linux/Mac 是 \n），确保每行独立
                writer.newLine()
            }
        }
    }
    fun writeTo(outputStream: FileOutputStream) {
        // 直接使用默认编码写入输出流（无法获知文件编码）
        OutputStreamWriter(outputStream, defaultEncoding).buffered().use { writer ->
            sequence.forEach { line ->
                // writer.write(line)写入当前行的文本内容（不含换行符）
                writer.write(line)
                // writer.newLine()写入系统相关的换行符（Windows 是 \r\n，Unix/Linux/Mac 是 \n），确保每行独立
                writer.newLine()
            }
        }
    }
}