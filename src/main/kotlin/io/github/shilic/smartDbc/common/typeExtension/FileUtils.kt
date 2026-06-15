package io.github.shilic.smartDbc.common.typeExtension

import java.io.File

/** 下一个可用文件, 会无限递增，直到找到不重复的文件(避免覆盖);
 *
 * 如果文件不存在，原样返回；
 *
 * 如果存在，自动递增文件名中的重复序号（如 file(1).txt → file(2).txt）, 若尚无序号则添加 (1)。
 */
fun File.nextAvailableFile(): File {
    if (!exists()) return this                     // 文件不存在，原样返回

    val base = nameWithoutExtension                // 官方 API
    val ext = extension.let { if (it.isEmpty()) "" else ".$it" }
    val parent = parentFile ?: File(".")

    // 如果原始文件名已带序号，从当前序号+1开始；否则从1开始
    val start = Regex("""\((?<num>\d+)\)$""").find(base)
        ?.groups["num"]?.value?.toInt()?.plus(1) ?: 1

    // 生成无限递增序号序列，找到第一个不存在的文件
    return generateSequence(start) { it + 1 }
        .map { File(parent, "${base}($it)$ext") }
        .first { !it.exists() }
}

/**
 * 下一个可用文件, 会无限递增，直到找到不重复的文件(避免覆盖);
 *
 * 字符串版本的扩展，行为与 File 版本一致。
 */
fun String.nextAvailablePath(): String {
    val file = File(this)
    return file.nextAvailableFile().absolutePath
}