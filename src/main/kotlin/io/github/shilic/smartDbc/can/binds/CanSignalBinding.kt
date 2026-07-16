package io.github.shilic.smartDbc.can.binds

/** 注解，用于将一个字段(属性)绑定到一个信号
 *
 * @param signalName  CAN 信号在 dbc 文件中的信号名称 ;
 * */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CanSignalBinding (
    /** 绑定 CAN 信号在 dbc 文件中的信号名称 */
    val signalName: String
)
