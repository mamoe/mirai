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
    BOTH,
    FALLBACK,
}

private val loaderType = when (systemProp("mirai.service.loader", "both")) {
    "jdk" -> LoaderType.JDK
    "both" -> LoaderType.BOTH
    "fallback" -> LoaderType.FALLBACK
    else -> throw IllegalStateException("cannot find a service loader, mirai.service.loader must be both, jdk or fallback (default by both)")
}

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String?): T {
    val fallbackService by lazy {
        Services.firstImplementationOrNull(Services.qualifiedNameOrFail(clazz)) as T?
    }

    val jdkService by lazy {
        ServiceLoader.load(clazz.java).firstOrNull() ?: ServiceLoader.load(clazz.java, clazz.java.classLoader)
            .firstOrNull()
    }

    var suppressed: Throwable? = null

    val services by lazy {
        when (loaderType) {
            LoaderType.JDK -> jdkService
            LoaderType.BOTH -> jdkService ?: fallbackService
            LoaderType.FALLBACK -> fallbackService
        } ?: if (fallbackImplementation != null) {
            runCatching { findCreateInstance<T>(fallbackImplementation) }.onFailure { suppressed = it }.getOrNull()
        } else null
    }

    return Services.getOverrideOrNull(clazz) ?: services
    ?: throw NoSuchElementException("Could not find an implementation for service class ${clazz.qualifiedName}").apply {
        if (suppressed != null) addSuppressed(suppressed)
    }
}

private fun <T : Any> findCreateInstance(fallbackImplementation: String): T {
    return Class.forName(fallbackImplementation).cast<Class<out T>>().kotlin.run { objectInstance ?: createInstance() }
}

public actual fun <T : Any> loadServiceOrNull(clazz: KClass<out T>, fallbackImplementation: String?): T? {
    return runCatching { loadService(clazz, fallbackImplementation) }.getOrNull()
}

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T> {
    val fallBackServicesSeq: Sequence<T> by lazy {
        Services.implementations(Services.qualifiedNameOrFail(clazz))?.map { it.value as T }.orEmpty().asSequence()
    }

    val jdkServices: Sequence<T> by lazy {
        sequence {
            val current = ServiceLoader.load(clazz.java).iterator()
            if (current.hasNext()) {
                yieldAll(current)
            } else {
                yieldAll(ServiceLoader.load(clazz.java, clazz.java.classLoader))
            }
        }
    }

    return when (loaderType) {
        LoaderType.JDK -> jdkServices
        LoaderType.BOTH -> jdkServices.plus(fallBackServicesSeq)
        LoaderType.FALLBACK -> fallBackServicesSeq
    }
}