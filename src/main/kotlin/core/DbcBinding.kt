package core

/** 用于绑定dbc文件 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DbcBinding(val value: Array<Dbc> = []) {
    /** 单个dbc文件 */
    annotation class Dbc(
        val dbcTag: String,
        val dbcPath: String
    )
}
