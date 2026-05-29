package io.github.shilic.smartDbc.can.binds

/** 注解，用于在字段(属性)上绑定信号。
 *
 * @param msgId  CAN报文ID ; 存在默认值，可不填写（填写后可提高效率）。
 * @param signalName  CAN 信号在 dbc 文件中的信号名称 ;
 * */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CanBinding (
    /** CAN 报文 ID，如 0x18ABAB01。存在默认值，可不填写（填写后可提高效率）。 */
    val msgId: Int = DEFAULT_ID,
    /** CAN 信号在 dbc 文件中的信号名称 */
    val signalName: String
) {
    companion object {
        /** CAN 报文 ID 的默认值 */
        const val DEFAULT_ID = -1
    }
}
