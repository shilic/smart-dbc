package toolTest

import io.github.shilic.smartDbc.common.typeExtension.nextAvailableFile
import io.github.shilic.smartDbc.common.typeExtension.nextAvailablePath
import java.io.File
import kotlin.test.Test
import kotlin.test.*

class FileUtilsTest {

    @Test
    fun `nextAvailableFile returns same file if not exists`() {
        val tmp = File.createTempFile("test_unique_", ".txt")
        tmp.delete() // ensure it doesn't exist
        try {
            val result = tmp.nextAvailableFile()
            assertEquals(tmp.absolutePath, result.absolutePath)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `nextAvailableFile increments when exists`() {
        val tmp = File.createTempFile("test_incr_", ".txt")
        try {
            // original exists → should get test_incr_xxx(1).txt
            val result = tmp.nextAvailableFile()
            assertTrue(result.name.contains("(1)"), "should add (1): ${result.name}")
        } finally {
            tmp.delete()
            // clean up the generated file
            val generated = File(tmp.parent, tmp.nameWithoutExtension + "(1).txt")
            generated.delete()
        }
    }

    @Test
    fun `nextAvailableFile increments existing number`() {
        val base = File.createTempFile("test_seq_", ".txt")
        val first = File(base.parent, "${base.nameWithoutExtension}(1).txt")
        try {
            first.createNewFile()
            val result = base.nextAvailableFile()
            assertTrue(result.name.contains("(2)"), "should increment to (2): ${result.name}")
        } finally {
            base.delete()
            first.delete()
            File(base.parent, "${base.nameWithoutExtension}(2).txt").delete()
        }
    }

    @Test
    fun `nextAvailablePath returns path string`() {
        val tmp = File.createTempFile("test_path_", ".tmp").also { it.delete() }
        try {
            val result = tmp.absolutePath.nextAvailablePath()
            assertEquals(tmp.absolutePath, result)
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `nextAvailableFile no extension`() {
        val tmp = File.createTempFile("test_noext_", "")
        tmp.delete()
        try {
            val result = tmp.nextAvailableFile()
            assertFalse(result.name.contains(".."), "should not double dot: ${result.name}")
        } finally {
            tmp.delete()
        }
    }
}
