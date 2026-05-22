package dataModel

import java.lang.reflect.Field

/** 通过反射设置和获取字段值 */
object FieldChanger {

    private const val ERR = "注解的字段类型出错，数据类型必须是 int,byte,short,float,double 及其包装类型"

    fun setFieldValue(field: Field, obj: Any?, sigValue: Double): Any? {
        if (obj == null) return null
        try {
            when (field.type) {
                Int::class.java, Int::class.javaObjectType -> field.set(obj, sigValue.toInt())
                Double::class.java, Double::class.javaObjectType -> field.set(obj, sigValue)
                Byte::class.java, Byte::class.javaObjectType -> field.set(obj, sigValue.toInt().toByte())
                Short::class.java, Short::class.javaObjectType -> field.set(obj, sigValue.toInt().toShort())
                Float::class.java, Float::class.javaObjectType -> field.set(obj, sigValue.toFloat())
                else -> error("$ERR，不支持的类型：${field.type.name}")
            }
        } catch (e: IllegalAccessException) {
            error("$ERR，访问失败：${field.type.name}")
        }
        return obj
    }

    fun getFieldValue(field: Field, obj: Any?): Double {
        if (obj == null) return 0.0
        try {
            return when (field.type) {
                Integer.TYPE -> field.getInt(obj).toDouble()
                java.lang.Double.TYPE -> field.getDouble(obj)
                java.lang.Byte.TYPE -> field.getByte(obj).toDouble()
                java.lang.Short.TYPE -> field.getShort(obj).toDouble()
                java.lang.Float.TYPE -> field.getFloat(obj).toDouble()
                else -> error(ERR)
            }
        } catch (e: IllegalAccessException) {
            error(ERR)
        }
    }
}
