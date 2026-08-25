package io.github.shilic.smartDbc.can.core

import io.github.shilic.smartDbc.can.contract.ICanIo
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.binding(canIo : ICanIo): T = canIo.binding(this)
fun <T : Any> Class<T>.binding(canIo : ICanIo): T = canIo.binding(this)