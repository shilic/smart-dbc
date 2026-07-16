package io.github.shilic.smartDbc.can.binds

/** 绑定一个CAN报文到类 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CanMessageBinding (
    /** 绑定 dbc 标签 */
    val dbcTag : String,
    /** 绑定 CAN 报文 ID，如 0x18ABAB01。 */
    val msgId: Int
)