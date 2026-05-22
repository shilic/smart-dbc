package core

/** 注解，用于绑定信号。messageId=CAN报文ID; signalTag=信号名称 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CanBinding(
    val messageId: Int = Default,
    val signalTag: String
) {
    companion object { const val Default = -1 }
}
