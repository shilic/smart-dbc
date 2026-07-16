package toolTest

import io.github.shilic.smartDbc.common.typeExtension.hasNoDuplicates
import kotlin.test.Test
import kotlin.test.*

class CollectionUtilsTest {

    @Test
    fun `hasNoDuplicates returns true for unique list`() {
        assertTrue(listOf("a", "b", "c").hasNoDuplicates())
    }

    @Test
    fun `hasNoDuplicates returns false for duplicates`() {
        assertFalse(listOf("a", "b", "a").hasNoDuplicates())
    }

    @Test
    fun `hasNoDuplicates empty list`() {
        assertTrue(emptyList<String>().hasNoDuplicates())
    }

    @Test
    fun `hasNoDuplicates single element`() {
        assertTrue(listOf("a").hasNoDuplicates())
    }

    @Test
    fun `hasNoDuplicates int duplicates`() {
        assertFalse(listOf(1, 2, 3, 2).hasNoDuplicates())
    }
}
