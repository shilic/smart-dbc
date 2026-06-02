package io.github.shilic.smartDbc.common.tool

import io.github.shilic.numberUtils.DataType
import io.github.shilic.smartDbc.dbc.dataModel.dataEnums.CanByteOrder

/** 将信号的字节排序转换为单纯的字节排序 */

val CanByteOrder.dataOrder :DataType get() = if (this == CanByteOrder.Intel) DataType.Intel else DataType.Motorola