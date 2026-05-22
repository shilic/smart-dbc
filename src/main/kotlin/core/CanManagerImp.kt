package core

import dataModel.models.CanDbc
import java.util.concurrent.ConcurrentHashMap

/**
 * CAN编解码器的管理器。实现了 dbc 绑定、报文编解码等方法。
 */
class CanManagerImp private constructor() : CanManagerService {

    private val dbcMap = ConcurrentHashMap<String, CanDbc>()
    private val coderMap = ConcurrentHashMap<String, CanCoder>()
    private val modelMap = ConcurrentHashMap<Class<*>, Any>()
    private var dbcInputInterface: DbcInputInterface? = null

    // ======================================== 绑定 ========================================

    override fun <T : CanCopyable<T>> bind(clazz: Class<T>): T? {
        if (!loadDbcAnnotations(clazz)) return null
        val instance = clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        modelMap.putIfAbsent(clazz, instance)
        bindModelFields(clazz, instance)
        println("Manager：绑定完成, class = ${clazz.name}")
        return instance
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : CanCopyable<T>> bind(model: T): T? {
        val clazz = model.javaClass as Class<T>
        if (!loadDbcAnnotations(clazz)) return null
        modelMap.putIfAbsent(clazz, model)
        bindModelFields(clazz, model)
        println("Manager：绑定完成, class = ${clazz.name}")
        return model
    }

    // ======================================== 模型操作 ========================================

    @Suppress("UNCHECKED_CAST")
    override fun <T : CanCopyable<T>> getModel(clazz: Class<T>): T? = modelMap[clazz] as? T

    override fun <T : CanCopyable<T>> createNewModel(clazz: Class<T>): T? = getModel(clazz)?.copyNew()

    // ======================================== 编解码 ========================================

    override fun deCode_B(canId: Int, data8: ByteArray) {
        getCoderForCanId(canId)?.deCode_B(canId, data8)
    }

    override fun enCode_B(canId: Int): ByteArray =
        getCoderForCanId(canId)?.enCode_B(canId) ?: ByteArray(8)

    override fun enCode_B(canId: Int, model: Any): ByteArray =
        getCoderForCanId(canId)?.enCode_B(canId, model) ?: ByteArray(8)

    // ======================================== DBC 管理 ========================================

    override fun addDbcInputInterface(dbcInputInterface: DbcInputInterface): CanManagerService {
        this.dbcInputInterface = dbcInputInterface
        println("Manager：DBC输入接口绑定完成")
        return this
    }

    override fun clearDBC(dbcTag: String) {
        dbcMap.remove(dbcTag); coderMap.remove(dbcTag)
        println("Manager：清理DBC：$dbcTag")
    }

    override fun clearAllDbc() { dbcMap.clear(); coderMap.clear(); println("Manager：清理所有DBC") }

    override fun clear() { clearAllDbc(); modelMap.clear(); println("Manager：清理所有绑定关系") }

    // ======================================== 私有方法 ========================================

    private fun loadDbcAnnotations(clazz: Class<*>): Boolean {
        val anno = clazz.getAnnotation(DbcBinding::class.java) ?: return false
        val dbcArray = anno.value
        if (dbcArray.isEmpty()) return false
        dbcArray.forEach { dbc ->
            if (!dbcMap.containsKey(dbc.dbcTag)) {
                addDbcToMap(dbc.dbcTag, dbc.dbcPath)
                println("Manager：DBC绑定成功，dbcTag = ${dbc.dbcTag}, dbcFilePath = ${dbc.dbcPath}")
            }
        }
        return true
    }

    private fun addDbcToMap(dbcTag: String, dbcFilePath: String): CanDbc {
        val dbc = dbcInputInterface?.let { iface ->
            iface.getInputStream(dbcFilePath).use { DbcParse.getDbcFromInputStream(dbcTag, it) }
        } ?: DbcParse.getDbcFromFilePath(dbcTag, dbcFilePath)
        dbcMap[dbcTag] = dbc
        return dbc
    }

    private fun bindModelFields(dataClass: Class<*>, model: Any) {
        for (field in dataClass.declaredFields) {
            field.isAccessible = true
            val canBind = field.getAnnotation(CanBinding::class.java) ?: continue
            val signal = resolveSignal(canBind)
                ?: error("字段 '${field.name}' 绑定有误: 信号{${canBind.signalTag}}未在DBC中找到")
            signal.field = field
            signal.dataModel = model
        }
    }

    private fun resolveSignal(bind: CanBinding) = dbcMap.values.firstNotNullOfOrNull { dbc ->
        if (bind.messageId == CanBinding.Default) dbc.getSignal(bind.signalTag.trim())
        else dbc.getSignal(bind.signalTag.trim(), bind.messageId)
    }

    private fun getCoderForCanId(canId: Int): CanCoder? {
        val dbc = dbcMap.values.firstOrNull { canId in it.intMsgMap } ?: return null
        return coderMap.getOrPut(dbc.dbcTag) { CanCoder(dbc) }
    }

    // ======================================== 单例 ========================================

    companion object {
        @Volatile private var instance: CanManagerImp? = null

        fun getInstance(): CanManagerImp = instance ?: synchronized(CanManagerImp::class.java) {
            instance ?: CanManagerImp().also {
                println("Manager：数据管理层初始化完成")
                instance = it
            }
        }
    }
}
