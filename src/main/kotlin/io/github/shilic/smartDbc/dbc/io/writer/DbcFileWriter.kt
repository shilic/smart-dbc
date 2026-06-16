package io.github.shilic.smartDbc.dbc.io.writer

import io.github.shilic.smartDbc.common.typeExtension.encoding
import io.github.shilic.smartDbc.common.typeExtension.nextAvailableFile
import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**  DBC文件写入器 */
class DbcFileWriter (
    /** 以只读方式传入一个只读DBC对象DataBaseCan, 防止副作用发生(kotlin风格的SDK) */
    val dbc: DataBaseCan,
    /** 默认文件编码：默认编码为GBK */
    val defaultEncoding: Charset = Charset.forName("GBK")
) {
    /** 惰性求值, 获取DBC文件的所有行 */
    val sequence: Sequence<String> get() = dbc.allSequence
    /** 安全地写入DBC文件, 避免文件已存在导致覆盖
     *
     * 自动判断文件编码: 如果文件存在，则尝试读取文件编码; 读取编码失败, 或输出文件不存在时，使用默认编码（GBK）*/
    fun safeWrite(path: String) = safeWrite(File(path))
    /** 安全地写入DBC文件, 避免文件已存在导致覆盖
     *
     * 自动判断文件编码: 如果文件存在，则尝试读取文件编码; 读取编码失败, 或输出文件不存在时，使用默认编码（GBK）*/
    fun safeWrite(file: File) = writeTo(file.nextAvailableFile())
    /** 将DBC对象输出至指定的文件;
     *
     * 自动判断文件编码: 如果文件存在，则尝试读取文件编码; 读取编码失败, 或输出文件不存在时，使用默认编码（GBK）
     *
     * */
    fun writeTo(path: String) = writeTo(File(path))
    /** 将DBC对象输出至指定的文件;
     *
     * 自动判断文件编码: 如果文件存在，则尝试读取文件编码; 读取编码失败, 或输出文件不存在时，使用默认编码（GBK）
     *
     * */
    fun writeTo(file: File) {
        // 自动判断文件编码: 如果文件存在，则尝试读取文件编码; 读取编码失败, 或输出文件不存在时，使用默认编码（GBK）
        val charset = if (file.exists()) { file.encoding ?: defaultEncoding } else { defaultEncoding }
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
    /** 将DBC对象输出至指定的文件输出流;
     *
     * 无法判断文件编码, 危险, 有乱码风险, 不建议使用
     *
     * */
    @Deprecated("不推荐使用这种方式输出, 有乱码风险", ReplaceWith("writeTo(File)"))
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