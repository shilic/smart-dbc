package io.github.shilic.smartDbc.common.typeExtension

import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/** 输入流提供者; 调用者通过反复调用该方法, 从而实现反复打开一个文件 */
typealias InputStreamProvider = () -> InputStream
/** 定义编码检测策略类型; 会调用输入流提供者得到文件流之后, 检测文件编码 */
typealias EncodingDetector = InputStreamProvider.() -> Charset?
val DefaultCharset : Charset = Charset.forName("GBK")
/** 获取文件编码 */
val File.encoding get() = { this@encoding.inputStream() }.detectEncoding()
/**文件编码检测器, 通过传入一个输入流提供者, 以及任意多个编码检测器;
 * 依次调用检测器进行检测, 返回第一个非空的编码结果, 如果所有检测器都返回null, 则返回null;
 * 函数会反复调用文件流提供者。
 * */
fun InputStreamProvider.detectEncoding (vararg detectors: EncodingDetector = arrayOf(universalDetector, bomDetector)): Charset? =
    detectors.asSequence().map { this@detectEncoding.it() }.firstOrNull { it != null }
/** BOM检测器 */
val bomDetector : EncodingDetector = {
    this().use { inputStream ->
        // 读取前4个字节检查BOM
        val bytes = ByteArray(4)
        val read = inputStream.read(bytes, 0, 4)
        if (read < 2) null
        when {
            // UTF-8 BOM: EF BB BF
            read >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> Charsets.UTF_8
            // UTF-16BE BOM: FE FF
            read >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> Charset.forName("UTF-16BE")
            // UTF-16LE BOM: FF FE
            read >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> Charset.forName("UTF-16LE")
            // UTF-32BE BOM: 00 00 FE FF
            read >= 4 && bytes[0] == 0x00.toByte() && bytes[1] == 0x00.toByte() && bytes[2] == 0xFE.toByte() && bytes[3] == 0xFF.toByte() -> Charset.forName("UTF-32BE")
            // UTF-32LE BOM: FF FE 00 00
            read >= 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() && bytes[2] == 0x00.toByte() && bytes[3] == 0x00.toByte() -> Charset.forName("UTF-32LE")
            else -> null
        }
    }
}
/** 通用编码检测器;
 *
 * 调用第三方库: UniversalDetector 进行文件编码的检测s */
val universalDetector : EncodingDetector = {
    this().use { inputStream ->
        val detector = UniversalDetector(null)
        val buffer = ByteArray(4096)
        var nRead: Int
        while (inputStream.read(buffer).also { nRead = it } > 0 && !detector.isDone) {
            detector.handleData(buffer, 0, nRead)
        }
        detector.dataEnd()
        // detector.reset()
        detector.detectedCharset?.let { Charset.forName(it) }
    }
}