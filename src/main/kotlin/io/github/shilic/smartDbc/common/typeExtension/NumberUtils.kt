package io.github.shilic.smartDbc.common.typeExtension

/**
 * 浮点型数值保留指定位数的小数
 */
fun Double.digitsFormat(digits: Int = 2): String = "%.${digits}f".format(this)