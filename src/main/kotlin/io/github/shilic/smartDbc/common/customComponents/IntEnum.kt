package io.github.shilic.smartDbc.common.customComponents

/**
 * 自引用泛型接口；
 *
 * 每个枚举类都有自己的fromInt方法, 用于快速从整型值获取枚举类实例;
 *
 * @param T 枚举类自身类型，用于实现类型安全的自引用
 */
interface IntEnum<T> where T : Enum<T>, T : IntEnum<T> {
    /**  枚举类的整型值 */
    val intValue: Int
    /**
     * 伴生对象必须实现这个接口才能提供fromInt方法
     */
    companion object {
        /**
         * 为所有实现IntEnum接口的枚举类的伴生对象添加扩展函数
         */
        inline fun <reified T> fromInt(value: Int): T where T : Enum<T>, T : IntEnum<T> =
            enumValues<T>().first { it.intValue == value }
        /**
         * 为所有实现IntEnum接口的枚举类的伴生对象添加扩展函数
         */
        inline fun <reified T> fromIntOrNull(value: Int): T? where T : Enum<T>, T : IntEnum<T> =
            enumValues<T>().firstOrNull { it.intValue == value }
    }
}
/**
 * 创建一个枚举类的实例
 *
 * @return 枚举类的实例
 */
inline fun <reified T> Int.toIntEnum(): T where T : Enum<T>, T : IntEnum<T> = IntEnum.fromInt<T>(this)
/**
 * 创建一个枚举类的实例
 *
 * @return 枚举类的实例
 */
inline fun <reified T> Int.toIntEnumOrNull(): T? where T : Enum<T>, T : IntEnum<T> = IntEnum.fromIntOrNull<T>(this)
