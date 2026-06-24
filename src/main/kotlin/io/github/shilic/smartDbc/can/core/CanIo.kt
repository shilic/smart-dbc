package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.*
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartDbc.valueConverter.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * 框架入口对象, 负责绑定数据模型和DBC, 通过接口兼容不同的底层CAN收发
 *
 * 使用单例模式, 对外提供统一入口
 */
object CanIo : IMcu {
    private val mcu: IMcu get() = mcuAdapter ?: error("没有注册 IMcu 服务，无法发送报文")
    /** 持有底层 [IMcu] 服务，需要由外部自行实例化，并注册到CanIo中。再通过该字段绑定监听事件。 */
    var mcuAdapter: IMcu? = null
    /** 持有只读DBC的可变集合(框架只需要只读DBC), 需要由外部自行实例化，并添加DBC进来 */
    val dbcMap: MutableMap<String, DataBaseCan> = mutableMapOf()
    /** 持有数据模型 */
    val modelMap: MutableMap<KClass<*>, Any> = mutableMapOf()

    /**
     * 绑定数据模型
     *
     * 绑定数据模型，绑定成功后，框架会自动将数据模型中的字段与DBC中的信号进行绑定，并保存到模型映射中。
     *
     * @param model 数据模型
     */
    inline fun <reified T : Any> bind(model: T) {
        // ------------------------- 前期校验 -------------------------
        val kClass : KClass<T> = T::class
        val dbcBind: DbcBinding = kClass.findAnnotation<DbcBinding>() ?: error("'${kClass.simpleName}'类型需要标记'${DbcBinding::class.simpleName}'注解, 才可以绑定")
        // 验证DBC必须先提前注册; 在已经注册的DBC标签中，搜索类型上标注的DBC标签; 要求标注的DBC必须注册进来。
        val missingDbcTags = dbcBind.dbcTags.filter { it !in dbcMap }
        require(missingDbcTags.isEmpty()) { "没有提前在${CanIo::class.simpleName}对象中注册以下DBC标签:${missingDbcTags}" }
        println("------------ 类型'${kClass.simpleName}'上绑定的${DbcBinding::class.simpleName}为: [${dbcBind.dbcTags.joinToString(",")}] ---------------")

        // 遍历所有字段, 然后执行绑定操作, 允许只读字段绑定
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
        println("---------------- 对象绑定完成, 已经成功将 '$kClass' 类型绑定至DBC中 -----------------")
    }

    // ================== 模型操作 ======================
    /** 获取绑定的模型 */
    inline fun <reified T : Any> getModel(): T? = modelMap[T::class] as? T
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
    fun send(msgId: Int, model: Any? = null) {
        val canFrame = findMessage(msgId)?.encodeCanFrame(model) ?: error("没有在注册DBC中找到报文ID:${CanMessage.msgIdToKey(msgId)} ")
        mcu.nativeSend(canFrame)
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

    override fun nativeSend(canFrame: CanFrame) = mcu.nativeSend(canFrame)

    override fun nativeRegister(canListener: CanListener) = mcu.nativeRegister(canListener)

    override fun nativeUnRegister() = mcu.nativeUnRegister()
}
