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

import android.graphics.Bitmap
import android.util.Base64
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

public actual typealias PlatformImage = Bitmap


public actual fun ByteArray.encodeBase64(): String {
    return Base64.encodeToString(this, Base64.DEFAULT)
}

public actual fun String.decodeBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

@PublishedApi
internal class StacktraceException(override val message: String?, private val stacktrace: Array<StackTraceElement>) :
    Exception(message, null, true, false) {
    override fun fillInStackTrace(): Throwable = this
    override fun getStackTrace(): Array<StackTraceElement> = stacktrace
}

public actual inline fun <reified E> Throwable.unwrap(): Throwable {
    if (this !is E) return this
    val e = StacktraceException("Unwrapped exception: $this", this.stackTrace)
    for (throwable in this.suppressed) {
        e.addSuppressed(throwable)
    }
    return this.findCause { it !is E }
        ?.also { it.addSuppressed(e) }
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