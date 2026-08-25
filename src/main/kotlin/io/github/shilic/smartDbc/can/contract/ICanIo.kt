package io.github.shilic.smartDbc.can.contract

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.core.CanIo
import io.github.shilic.smartDbc.can.models.canFrame.contract.*
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartDbc.valueConverter.*
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.safeCast

interface ICanIo : IMcu {
    val logTag : String
    /** 持有底层 MCU适配器 ，需要由外部自行实例化，并注册到CanIo中。再通过该字段绑定监听事件。 */
    var mcuAdapter: IMcu
    /** 持有只读DBC的可变集合(框架只需要只读DBC), 需要由外部自行实例化，并添加DBC进来 */
    val dbcMap: Map<String, DataBaseCan>
    /** 持有数据模型 */
    val modelMap: Map<KClass<*>, Any>
    /** Java 兼容版：接受 Class<T> 绑定 */
    fun <T : Any> binding(clazz: Class<T>): T  {
        val kClass :KClass<T> = clazz.kotlin
        return binding(kClass)
    }
    fun <T : Any> binding(kClass : KClass<T>) : T {
        val model : T = try {
            kClass.createInstance()
        } catch (_ : IllegalArgumentException) {
            error("传入框架的数据类 ${kClass.simpleName} 需要有一个无参构造。")
        }
        val msgBind: CanMessageBinding = kClass.findAnnotation<CanMessageBinding>()
            ?: error("'${kClass.simpleName}'类型需要标记'${CanMessageBinding::class.simpleName}'注解, 才可以绑定")
        // 验证DBC必须先提前注册; 在已经注册的DBC标签中，搜索类型上标注的DBC标签; 要求标注的DBC必须注册进来。
        val dbc = CanIo.dbcMap[msgBind.dbcTag] ?: error("没有提前在${CanIo::class.simpleName}对象中注册以下DBC标签:${msgBind.dbcTag}")
        val msg = dbc[msgBind.msgId] ?: error("没有在注册DBC中找到 报文ID:${CanMessage.msgIdToKey(msgBind.msgId)}")

        // ------------- 遍历所有字段, 然后执行绑定操作, 允许只读字段绑定 ------------------
        kClass.memberProperties.forEach { property ->
            // 拿到字段上的绑定信息，没有就跳过这一次循环。
            val sigBind = property.findAnnotation<CanSignalBinding>() ?: return@forEach
            // 使用绑定信息，到DBC中进行查找，查找到对应的信号
            val signal: CanSignal = msg[sigBind.signalName]
                ?: error("没有在注册DBC中找到 字段'${property}'的'${CanSignalBinding::class.simpleName}'注解上标注的信号:${sigBind.signalName}")
            // 将持有者和字段绑定到DBC对象中
            signal.let {
                it.originalOwnerType = kClass
                it.originalOwner = model
                it.originalProperty = property
            }
            // 保存绑定好的数据模型
            CanIo.modelMap[kClass] = model
        }
        println("${CanIo.logTag}: 对象绑定完成, 已经成功将 '$kClass' 类型绑定至DBC中")
        return model
    }
    // ================== 模型操作 ======================
    /** 获取绑定的模型 */
    fun <T : Any> getModel(clazz : Class<T>): T? = clazz.kotlin.safeCast(modelMap[clazz.kotlin])
    /** 获取绑定的模型 */
    fun <T : Any> getModel(kClass : KClass<T>): T? = kClass.safeCast(modelMap[kClass])
    // ======================= 发送报文 =========================
    /** 发送报文
     *
     * 读取CAN值：
     *
     * -> 优先从指定接受者字段读取值; [model] 参数为空时，使用默认接受者
     *
     * -> 如果绑定字段值为空, 其次从DBC对象读取信号值
     *
     * @param msgId 报文ID
     * @param model 数据模型
     *  */
    fun transmit(msgId: Int, model: Any? = null) {
        val canFrame = findMessage(msgId)?.encodeCanFrame(model) ?: error("没有在注册DBC中找到报文ID:${CanMessage.msgIdToKey(msgId)} ")
        mcuAdapter.transmit(canFrame)
    }
    /** 解码CAN报文 */
    fun decodeCanFrame(canFrame: CanFrame) = findMessage(canFrame.msgId)?.decodeCanFrame(canFrame)
    /**  查找CanMessage报文  */
    fun findMessage(msgId: Int): CanMessage? = dbcMap.values.firstNotNullOfOrNull { dbc -> dbc[msgId] }
    /**  通过字段的绑定信息，查找信号  */
    fun findSignal(canBind: CanBinding): CanSignal? = when (canBind.msgId) {
        // 如果没有指定报文ID，则直接使用信号名称从DBC中查找信号，查询效率会低一些
        CanBinding.DEFAULT_ID -> dbcMap.values.firstNotNullOfOrNull { dbc -> dbc.getSignal(canBind.signalName) }
        // 使用报文ID + 信号名称，快速查找一个信号；速度会快很多。
        else -> dbcMap.values.firstNotNullOfOrNull { dbc -> dbc.getSignal(canBind.msgId, canBind.signalName) }
    }
    override fun transmit(canFrame: CanFrame) = mcuAdapter.transmit(canFrame)
    override fun register(canListener: CanListener) = mcuAdapter.register(canListener)
    override fun unRegister(canListener: CanListener) = mcuAdapter.unRegister(canListener)
    override fun unRegisterAll() = mcuAdapter.unRegisterAll()
}