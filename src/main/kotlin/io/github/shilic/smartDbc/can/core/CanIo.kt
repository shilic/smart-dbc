package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/**
 * CAN IO 兼容层，兼容不同的底层CAN收发实现。
 *
 * 对外提供统一入口
 */
object CanIo {
    private val mcu: IMcu get() = mMcu ?: error("没有注册 IMcu 服务，无法发送报文")
    /** 持有底层 [IMcu] 服务，需要由外部自行实例化，并注册到CanIo中 */
    var mMcu: IMcu? = null
    /** 持有只读DBC的集合(框架只需要只读DBC), 需要由外部自行实例化，并添加DBC进来 */
    val dbcMap: MutableMap<String, DataBaseCan> = mutableMapOf()
    /** 持有数据模型 */
    val modelMap : MutableMap<KClass<*>, Any> = mutableMapOf()

    inline fun <reified T : Any> bind(model: T) {
        // ------------------------- 前期校验 -------------------------
        val kClass : KClass<T> = T::class
        require(kClass.isSubclassOf(CanCopyable::class)) {
            "'${kClass.simpleName}'类型需要实现'${CanCopyable::class.simpleName}'接口"}
        val dbcBind: DbcBinding = kClass.findAnnotation<DbcBinding>()
            ?: error("'${kClass.simpleName}'类型需要标记'${DbcBinding::class.simpleName}'注解")
        // 验证DBC必须先提前注册
        val missingDbcTags = dbcBind.dbcTags.filter { it !in dbcMap }
        require(missingDbcTags.isEmpty()) {
            "没有提前在${CanIo::class.simpleName}对象中注册以下DBC标签:${missingDbcTags.joinToString(", ")}" }

        // 遍历所有字段, 然后执行绑定操作, 允许只读字段绑定
        kClass.memberProperties.forEach { property ->
            // 拿到字段上的绑定信息，没有就跳过这一次循环。
            val canBind: CanBinding = property.findAnnotation<CanBinding>() ?: return@forEach
            // 使用绑定信息，到DBC中进行查找，查找到对应的信号
            val signal: CanSignal = findSignal(canBind)
                ?: error("没有在注册DBC中找到 字段'${property}'的'${CanBinding::class.simpleName}'注解上标注的信号:${canBind.signalName}")
            // 将持有者和字段绑定到DBC对象中
            signal.let {
                it.owner = model
                it.property = property
            }
            // 保存绑定好的数据模型
            modelMap[kClass] = model
        }
    }

    // ================== 模型操作 ======================
    /** 获取绑定的模型 */
    inline fun <reified T : Any> getModel(): T? = modelMap[T::class] as? T
    /**
     * 从模型映射中创建指定类型的新实例。
     * 要求目标类型实现 [CanCopyable] 接口，并通过 [CanCopyable.copyNew] 方法创建新实例。
     *
     * @param T 要创建的类型，必须实现 [CanCopyable] 接口
     * @return 新创建的实例，如果类型不匹配或创建失败则返回 null
     */
    inline fun <reified T : Any> copyNewModel(): T? = (modelMap[T::class] as? CanCopyable<*>)?.copyNew() as? T
    // ======================================== 发送报文 ========================================

//    override fun send(canId: Int, model: Any) {
//        checkNotNull(mcu) { "没有注册CAN服务，无法发送报文" }
//        mcu!!.nativeSend(canId, manager.enCode_B(canId, model))
//    }

    /**  查找信号  */
    fun findSignal(canBind: CanBinding): CanSignal? = when (canBind.msgId) {
        CanBinding.DEFAULT_ID -> dbcMap.values.firstNotNullOfOrNull { dbc -> dbc.getSignal(canBind.signalName) }
        else -> dbcMap.values.firstNotNullOfOrNull { dbc -> dbc.getSignal(canBind.msgId, canBind.signalName) }
    }
}
