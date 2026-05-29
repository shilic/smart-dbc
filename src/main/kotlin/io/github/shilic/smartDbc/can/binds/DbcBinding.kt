package io.github.shilic.smartDbc.can.binds

/** 用于绑定dbc文件 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DbcBinding (
    /** dbcTag 标签的数组集合 */
    val dbcTags: Array<String> = []
)
