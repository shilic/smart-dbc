package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import kotlin.reflect.KClass
import kotlin.reflect.full.*

/**
 * 框架入口对象, 负责绑定数据模型和DBC, 通过接口兼容不同的底层CAN收发
 *
 * 使用单例模式, 对外提供统一入口
 */
object CanIo: ICanIo {
    override val logTag : String = "${CanIo::class.simpleName}"
    /** 持有底层 MCU适配器 ，需要由外部自行实例化，并注册到CanIo中。再通过该字段绑定监听事件。 */
    override var mcuAdapter : IMcu = DefaultMcuAdapter
    /** 持有只读DBC的可变集合(框架只需要只读DBC), 需要由外部自行实例化，并添加DBC进来 */
    override val dbcMap : MutableMap<String, DataBaseCan> = mutableMapOf()
    /** 持有数据模型 */
    override val modelMap: MutableMap<KClass<*>, Any> = mutableMapOf()
    
    /**
     * 绑定数据模型;
     *
     * 使用 [DbcBinding] 注解和 [CanBinding] 注解进行绑定。
     *
     * 绑定成功后，框架会自动将数据模型中的字段与DBC中的信号进行绑定。
     *
     * @param model 数据模型
     * @Deprecated 使用 binding 替代
     */
    @Deprecated("使用 binding 替代", ReplaceWith("binding(kClass)"))
    inline fun <reified T : Any> bind(model: T) {
        // ------------------------- 前期校验 -------------------------
        val kClass : KClass<T> = T::class
        val dbcBind: DbcBinding = kClass.findAnnotation<DbcBinding>()
            ?: error("'${kClass.simpleName}'类型需要标记'${DbcBinding::class.simpleName}'注解, 才可以绑定")
        // 验证DBC必须先提前注册; 在已经注册的DBC标签中，搜索类型上标注的DBC标签; 要求标注的DBC必须注册进来。
        val missingDbcTags = dbcBind.dbcTags.filter { it !in dbcMap }
        require(missingDbcTags.isEmpty()) { "没有提前在${CanIo::class.simpleName}对象中注册以下DBC标签:${missingDbcTags}" }

        // ------------- 遍历所有字段, 然后执行绑定操作, 允许只读字段绑定(只读字段无法写入，但是可以读取) ------------------
        kClass.memberProperties.forEach { property ->
            // 拿到字段上的绑定信息，没有就跳过这一次循环。
            val canBind = property.findAnnotation<CanBinding>() ?: return@forEach
            // 使用绑定信息，到DBC中进行查找，查找到对应的信号
            val signal: CanSignal = findSignal(canBind)
                ?: error("没有在注册DBC中找到 字段'${property}'的'${CanBinding::class.simpleName}'注解上标注的信号:${canBind.signalName}")
            // 将持有者和字段绑定到DBC对象中
            signal.let {
                it.originalOwnerType = kClass
                it.originalOwner = model
                it.originalProperty = property
            }
            // 保存绑定好的数据模型
            modelMap[kClass] = model
        }
        println("$logTag: 对象绑定完成, 已经成功将 '$kClass' 类型绑定至DBC中")
    }
    /**
     * 绑定数据模型;
     *
     * 使用 [CanMessageBinding] 注解和 [CanSignalBinding] 注解进行绑定。
     *
     * 绑定成功后，框架会自动将数据模型中的字段与DBC中的信号进行绑定。
     *
     * @param model 数据模型
     */
    @Deprecated("使用 binding 替代", ReplaceWith("binding(kClass)"))
    inline fun <reified T : Any> binding(model: T) {
        binding(T::class)
    }
    // ================== 模型操作 ======================
    /** 获取绑定的模型 */
    @Deprecated("请使用getModel(kClass : KClass<T>)替代", ReplaceWith("getModel(kClass)"))
    inline fun <reified T : Any> getModel(): T? = modelMap[T::class] as? T
}
