/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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

