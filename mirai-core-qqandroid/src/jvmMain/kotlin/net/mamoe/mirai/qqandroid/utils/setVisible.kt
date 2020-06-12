package net.mamoe.mirai.qqandroid.utils

import java.lang.reflect.Modifier
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

internal actual fun KProperty1<*, *>.getValueAgainstPermission(receiver: Any): Any? {
    return this.javaField?.apply { isAccessible = true }?.get(receiver)
}

// on JVM, it will be resolved to member function
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual internal inline fun <reified T : Annotation> KProperty<*>.hasAnnotation(): Boolean =
    findAnnotation<T>() != null


// on JVM, it will be resolved to member function
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual internal fun KProperty<*>.isTransient(): Boolean =
    javaField?.modifiers?.and(Modifier.TRANSIENT) != 0

