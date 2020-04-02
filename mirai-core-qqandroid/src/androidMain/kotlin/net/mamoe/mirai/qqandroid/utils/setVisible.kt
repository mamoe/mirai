package net.mamoe.mirai.qqandroid.utils

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

internal actual fun KProperty1<*, *>.getValueAgainstPermission(receiver: Any): Any? {
    return this.javaField?.apply { isAccessible = true }?.get(receiver)
}