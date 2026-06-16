package io.github.shilic.smartDbc.can.accessors

import io.github.shilic.smartDbc.common.typeExtension.toDoubleValue
import io.github.shilic.smartDbc.common.typeExtension.toPropertyValue
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * 属性访问器
 *
 * 持有特定的拥有者对象和属性
 *
 * 用于将单个信号绑定到一个字段后，可以通过这个属性访问器，获取和设置字段值。
 */
interface KPropertyAccessor {
    // +++++++++++++++++ 需要使用者自行去实现的属性 +++++++++++++++++
    /** 原始拥有者对象类型 */
    var originalOwnerType: KClass<*>?
    /** 原始拥有者对象(可为空) */
    var originalOwner: Any?
    /** 原始字段引用(可为空) */
    var originalProperty: KProperty1<*, *>?

    // ------------------------ 预定义好的属性和方法 -----------------------
    /**  获取字段类型 */
    val propertyType: KClass<*> get() = originalProperty!!.returnType.classifier as? KClass<*> ?: Any::class
    /**  设置指定接受者的字段值; 参数为空时，使用默认接受者
     *
     * 注意：只有可变字段才能设置!! 只有字段和接受者不为空时才会设置!!
     *
     * 数据类型，只有 Unsigned、Signed、Float、Double 四种; 故外部干脆直接统一成 Double 类型，再转换为对应的数据类型，最后再进行设置。
     * */
    fun setPropertyValue(value: Double, newOwner: Any? = null) {
        var aOwner: Any? = newOwner ?: originalOwner
        // 只有三个都不为空，才会继续执行后面的代码
        if (originalProperty == null || aOwner == null || originalOwnerType == null) { return }
        // 在非空的情况下，判断类型是否一致
        require(originalOwnerType!! == aOwner::class) {"属性拥有者类型出错，属性拥有者类型必须与原始拥有者类型一致; " +
                "你传入的类型为: ${aOwner::class.simpleName}, 实际应该为 ${originalOwnerType!!.simpleName}"}
        val mProperty = originalProperty!!
        mProperty.isAccessible = true
        if (originalProperty !is KMutableProperty1<*, *>) { return }
        val setter : Setter<*, *> = (mProperty as KMutableProperty1<*, *>).setter
        val safeValue: Comparable<*> = value.toPropertyValue(propertyType)
        setter.call(aOwner, safeValue)
    }
    /** 获取指定接受者的对应字段值; 参数为空时，使用默认接受者
     *
     * 一个CAN信号绑定至一个字段之后，使用者直接给字段赋值，然后框架通过 getPropertyValue() 获取字段值，再自动编码到CAN报文中。
     *
     * 如果没有绑定字段和接受者，则返回 null ;*/
    fun getPropertyValue(newOwner: Any? = null): Double? {
        var aOwner: Any? = newOwner ?: originalOwner
        // 只有三个都不为空，才会继续执行后面的代码
        if (originalProperty == null || aOwner == null || originalOwnerType == null) { return null }
        // 在非空的情况下，判断类型是否一致
        require(originalOwnerType!! == aOwner::class) {"属性拥有者类型出错，属性拥有者类型必须与原始拥有者类型一致; " +
                "你传入的类型为: ${aOwner::class.simpleName}, 实际应该为 ${originalOwnerType!!.simpleName}"}
        val mProperty = originalProperty!!
        mProperty.isAccessible = true
        val value: Any? = mProperty.getter.call(aOwner)
        return value?.toDoubleValue()
    }
}