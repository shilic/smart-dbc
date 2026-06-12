package io.github.shilic.smartDbc.common.typeExtension

import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/** 1. 定义编码检测策略类型 */
typealias EncodingDetector = (FileInputStream) -> Charset?

/** 2. 函数式工具：尝试多个检测器，返回第一个有效结果 */
fun firstDetected(vararg detectors: EncodingDetector): EncodingDetector = { fileInputStream ->
    detectors.asSequence()
        .map { detectSafe(it)(fileInputStream) }
        .firstOrNull { it != null }
}
/** 安全执行检测，确保流被重置 */
fun detectSafe(detector: EncodingDetector): EncodingDetector = { fileInputStream ->
    val originalPosition = fileInputStream.channel.position()
    try {
        // 每次检测前重置位置
        fileInputStream.channel.position(originalPosition)
        detector(fileInputStream)
    } catch (_: Exception) {
        null
    } finally {
        // 确保重置流位置
        try {
            fileInputStream.channel.position(originalPosition)
        } catch (_: Exception) {
            // 静默失败
        }
    }
}
/** BOM检测器 */
val bomDetector: EncodingDetector = { fileInputStream ->
    // 读取前4个字节检查BOM
    val bytes = ByteArray(4)
    val read = fileInputStream.read(bytes, 0, 4)
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
/** 通用编码检测器 */
val universalDetector : EncodingDetector = { fileInputStream ->
    val detector = UniversalDetector(null)
    val buffer = ByteArray(4096)
    var nRead: Int
    while (fileInputStream.read(buffer).also { nRead = it } > 0 && !detector.isDone) {
        detector.handleData(buffer, 0, nRead)
    }
    detector.dataEnd()
    detector.detectedCharset?.let { Charset.forName(it) }
}
/** 4. 组合检测器 */
val defaultDetector: EncodingDetector = firstDetected(universalDetector, bomDetector)

/**  获取文件的编码; 获取后，会回到文件指针原始位置 */
val File.encoding: Charset? get() = this.inputStream().encoding
/**  获取文件流的编码; 获取后，会回到文件指针原始位置 */
val FileInputStream.encoding: Charset? get() = defaultDetector(this)