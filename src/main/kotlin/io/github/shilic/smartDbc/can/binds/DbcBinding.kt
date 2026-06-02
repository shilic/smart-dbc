package io.github.shilic.smartDbc.can.binds

/** 用于绑定dbc文件
 *
 * 使用数组的方式，在类型上定义这个类型包含哪些DBC文件
 * */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DbcBinding (
    /** dbcTag 标签的数组集合 */
    val dbcTags: Array<String> = []
)
