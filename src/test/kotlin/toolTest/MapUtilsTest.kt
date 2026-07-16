package toolTest

import io.github.shilic.smartDbc.common.typeExtension.findFirstKeyByValue
import io.github.shilic.smartDbc.common.typeExtension.findKeysByValue
import kotlin.test.Test
import kotlin.test.*

class MapUtilsTest {

    @Test
    fun `findKeysByValue single match`() {
        val map = mapOf("a" to 1, "b" to 2, "c" to 3)
        assertEquals(setOf("b"), map.findKeysByValue(2))
    }

    @Test
    fun `findKeysByValue multiple matches`() {
        val map = mapOf("a" to 1, "b" to 1, "c" to 2)
        assertEquals(setOf("a", "b"), map.findKeysByValue(1))
    }

    @Test
    fun `findKeysByValue no match returns empty set`() {
        val map = mapOf("a" to 1, "b" to 2)
        assertEquals(emptySet(), map.findKeysByValue(99))
    }

    @Test
    fun `findKeysByValue empty map`() {
        val map = emptyMap<String, Int>()
        assertEquals(emptySet(), map.findKeysByValue(1))
    }

    @Test
    fun `findFirstKeyByValue found`() {
        val map = mapOf("a" to 1, "b" to 2, "c" to 3)
        assertEquals("b", map.findFirstKeyByValue(2))
    }

    @Test
    fun `findFirstKeyByValue not found returns null`() {
        val map = mapOf("a" to 1)
        assertNull(map.findFirstKeyByValue(99))
    }
}
