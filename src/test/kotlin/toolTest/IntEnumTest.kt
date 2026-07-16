package toolTest

import io.github.shilic.smartDbc.common.customComponents.IntEnum
import io.github.shilic.smartDbc.common.customComponents.toIntEnum
import io.github.shilic.smartDbc.common.customComponents.toIntEnumOrNull
import kotlin.test.Test
import kotlin.test.*

enum class TestEnum(override val intValue: Int) : IntEnum<TestEnum> {
    Alpha(0), Beta(1), Gamma(2);
}

class IntEnumTest {

    @Test
    fun `fromInt returns correct enum`() {
        assertEquals(TestEnum.Alpha, IntEnum.fromInt<TestEnum>(0))
        assertEquals(TestEnum.Beta, IntEnum.fromInt<TestEnum>(1))
        assertEquals(TestEnum.Gamma, IntEnum.fromInt<TestEnum>(2))
    }

    @Test
    fun `fromInt throws on invalid value`() {
        assertFailsWith<NoSuchElementException> { IntEnum.fromInt<TestEnum>(99) }
    }

    @Test
    fun `fromIntOrNull valid`() {
        assertEquals(TestEnum.Alpha, IntEnum.fromIntOrNull<TestEnum>(0))
    }

    @Test
    fun `fromIntOrNull invalid returns null`() {
        assertNull(IntEnum.fromIntOrNull<TestEnum>(99))
    }

    @Test
    fun `toIntEnum extension`() {
        val result: TestEnum = 1.toIntEnum()
        assertEquals(TestEnum.Beta, result)
    }

    @Test
    fun `toIntEnumOrNull extension valid`() {
        assertEquals(TestEnum.Gamma, 2.toIntEnumOrNull<TestEnum>())
    }

    @Test
    fun `toIntEnumOrNull extension invalid returns null`() {
        assertNull(99.toIntEnumOrNull<TestEnum>())
    }
}
