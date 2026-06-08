package io.github.shilic.smartDbc.common.typeExtension

import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/** 1. 定义编码检测策略类型 */
typealias EncodingDetector = (FileInputStream) -> Charset?

/** 2. 函数式工具：尝试多个检测器，返回第一个有效结果 */
fun firstDetected(vararg detectors: EncodingDetector): EncodingDetector = { file ->
    detectors.asSequence()
        .map { it(file) }
        .firstOrNull { it != null }
}

/** BOM检测器 */
val bomDetector: EncodingDetector = { fileInputStream ->
    try {
        // 保存当前位置
        val originalPosition = fileInputStream.channel.position()

        // 读取前4个字节检查BOM
        val bytes = ByteArray(4)
        val read = fileInputStream.read(bytes)
        if (read < 2) null

        // 恢复到原始位置
        fileInputStream.channel.position(originalPosition)
        when {
            bytes.size >= 3 && bytes[0] == 0xEF.toByte() &&
                    bytes[1] == 0xBB.toByte() &&
                    bytes[2] == 0xBF.toByte() -> Charsets.UTF_8
            bytes.size >= 2 && bytes[0] == 0xFE.toByte() &&
                    bytes[1] == 0xFF.toByte() -> Charset.forName("UTF-16BE")
            bytes.size >= 2 && bytes[0] == 0xFF.toByte() &&
                    bytes[1] == 0xFE.toByte() -> Charset.forName("UTF-16LE")
            else -> null
        }
    }
    catch (_: Exception){
         null
    }
}
/** 通用编码检测器 */
val universalDetector : EncodingDetector = { fileInputStream ->
    try {
        // 保存当前位置
        val originalPosition = fileInputStream.channel.position()
        val detector = UniversalDetector(null)
        val buffer = ByteArray(4096)
        var nRead: Int
        while (fileInputStream.read(buffer).also { nRead = it } > 0 && !detector.isDone) {
            detector.handleData(buffer, 0, nRead)
        }
        detector.dataEnd()
        // 恢复到原始位置
        fileInputStream.channel.position(originalPosition)
        detector.detectedCharset?.let { Charset.forName(it) }
    } catch (_: Exception) {
        null
    } finally {
        // 确保重置流位置
        try {
            fileInputStream.channel.position(0)
        } catch (_: Exception) {
            // 忽略重置失败
        }
    }
}
/** 4. 组合检测器 */
val defaultDetector: EncodingDetector = firstDetected(universalDetector, bomDetector)

/**  获取文件的编码 */
val File.encoding: Charset? get() = defaultDetector(this.inputStream())
/**  获取文件的编码 */
val FileInputStream.encoding: Charset? get() = defaultDetector(this)