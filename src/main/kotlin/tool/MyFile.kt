package tool

import java.io.File
import java.io.IOException

/** 文件工具类 */
object MyFile {

    /** 创建一个不重复的文件。存在则重命名 */
    fun newFile(filePath: String): File? {
        val file = File(filePath)
        return if (file.exists()) {
            println("文件存在，重命名新创建的文件")
            newFile(newFilePath(filePath))
        } else {
            println("文件不存在！系统创建新文件")
            try { file.takeIf { it.createNewFile() } } catch (e: IOException) {
                println("文件操作发生异常：${e.message}"); null
            }
        }
    }

    /** 得到一个不重复的文件链接 */
    fun newFilePath(filePath: String): String {
        val file = File(filePath)
        return if (file.exists()) {
            println("文件存在，重命名新创建新的文件链接")
            newFilePath(generateNewPath(file))
        } else {
            println("文件不存在！系统创建新的文件链接")
            filePath
        }
    }

    private fun generateNewPath(file: File): String {
        val parentPath = file.parent
        val fileName = file.name
        val dotIndex = fileName.lastIndexOf(".")
        val ext = fileName.substring(dotIndex)
        val nameWithoutExt = fileName.substring(0, dotIndex)
        return "$parentPath\$nameWithoutExt(new)$ext"
    }
}
