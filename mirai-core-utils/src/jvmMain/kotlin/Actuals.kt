/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import java.awt.image.BufferedImage
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

public actual typealias PlatformImage = BufferedImage

public actual fun ByteArray.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}

public actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

public actual inline fun <reified E> Throwable.unwrap(): Throwable {
    if (this !is E) return this
    return this.findCause { it !is E }
        ?.also { it.addSuppressed(this) }
        ?: this
}

public actual fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String?): T {
    var suppressed: Throwable? = null
    return ServiceLoader.load(clazz.java).firstOrNull()
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
        ?: if (fallbackImplementation == null) return null
        else runCatching { findCreateInstance<T>(fallbackImplementation) }.getOrNull()
}