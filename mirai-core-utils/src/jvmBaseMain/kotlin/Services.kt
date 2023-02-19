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

private enum class LoaderType {
    JDK,
    FALLBACK,
}

private val loaderType = when (systemProp("mirai.service.loader", "jdk")) {
    "jdk" -> LoaderType.JDK
    "fallback" -> LoaderType.FALLBACK
    else -> throw IllegalStateException("mirai.service.loader must be jdk or fallback, cannot find a service loader")
}

private fun <T : Any> getJDKServices(clazz: KClass<out T>): ServiceLoader<out T> = ServiceLoader.load(clazz.java)

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String?): T {
    fun getFALLBACKService(clazz: KClass<out T>) =
        (Services.firstImplementationOrNull(Services.qualifiedNameOrFail(clazz)) as T?)

    var suppressed: Throwable? = null

    val services = when (loaderType) {
        LoaderType.JDK -> getJDKServices(clazz).firstOrNull() ?: getFALLBACKService(clazz)
        LoaderType.FALLBACK -> getFALLBACKService(clazz) ?: getJDKServices(clazz).firstOrNull()
    } ?: if (fallbackImplementation != null) {
        runCatching { findCreateInstance<T>(fallbackImplementation) }.onFailure { suppressed = it }.getOrNull()
    } else null

    return services
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

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T> {
    val seq: Sequence<T> =
        Services.implementations(Services.qualifiedNameOrFail(clazz))?.map { it.value as T }.orEmpty().asSequence()

    return when (loaderType) {
        LoaderType.JDK -> getJDKServices(clazz).asSequence().plus(seq)
        LoaderType.FALLBACK -> seq
    }
}