package core

/** CAN报文编解码器的管理器接口 */
interface CanManagerService {
    fun <T : CanCopyable<T>> bind(clazz: Class<T>): T?
    fun <T : CanCopyable<T>> bind(model: T): T?
    fun <T : CanCopyable<T>> getModel(clazz: Class<T>): T?
    fun <T : CanCopyable<T>> createNewModel(clazz: Class<T>): T?
    fun deCode_B(canId: Int, data8: ByteArray)
    fun enCode_B(canId: Int): ByteArray
    fun enCode_B(canId: Int, model: Any): ByteArray
    fun addDbcInputInterface(dbcInputInterface: DbcInputInterface): CanManagerService
    fun clearDBC(dbcTag: String)
    fun clearAllDbc()
    fun clear() { clearAllDbc() }
}
