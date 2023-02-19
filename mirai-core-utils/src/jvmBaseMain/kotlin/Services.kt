/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

public actual fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String?): T {
    var suppressed: Throwable? = null
    return ServiceLoader.load(clazz.java).firstOrNull()
        ?: ServiceLoader.load(clazz.java, clazz.java.classLoader).firstOrNull()
        ?: (if (fallbackImplementation == null) null
        else runCatching { findCreateInstance<T>(fallbackImplementation) }.onFailure { suppressed = it }.getOrNull())
        ?: throw NoSuchElementException("Could not find an implementation for service class ${clazz.qualifiedName}").apply {
            if (suppressed != null) addSuppressed(suppressed)
        }
}

private fun <T : Any> findCreateInstance(fallbackImplementation: String): T {
    return Class.forName(fallbackImplementation).cast<Class<out T>>().kotlin.run { objectInstance ?: createInstance() }
}

public actual fun <T : Any> loadServiceOrNull(clazz: KClass<out T>, fallbackImplementation: String?): T? {
    return ServiceLoader.load(clazz.java).firstOrNull()
        ?: ServiceLoader.load(clazz.java, clazz.java.classLoader).firstOrNull()
        ?: if (fallbackImplementation == null) return null
        else runCatching { findCreateInstance<T>(fallbackImplementation) }.getOrNull()
}

public actual fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T> {
    return sequence {
        val current = ServiceLoader.load(clazz.java).iterator()
        if (current.hasNext()) {
            yieldAll(current)
        } else {
            yieldAll(ServiceLoader.load(clazz.java, clazz.java.classLoader))
        }
    }
}