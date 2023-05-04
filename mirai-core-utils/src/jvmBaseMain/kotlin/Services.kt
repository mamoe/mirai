/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
        ServiceLoader.load(clazz.java).firstOrNull()?.let { return@lazy it }

        ServiceLoader.load(clazz.java, clazz.java.classLoader).firstOrNull()
    }

    var suppressed: Throwable? = null

    val services by lazy {
        when (loaderType) {
            LoaderType.JDK -> jdkService
            LoaderType.BOTH -> jdkService ?: fallbackService
            LoaderType.FALLBACK -> fallbackService
        }?.let { return@lazy it }

        if (fallbackImplementation != null) {
            runCatching {
                findCreateInstance<T>(fallbackImplementation)
            }.onFailure { suppressed = it }.getOrNull()
        } else null
    }

    return Services.getOverrideOrNull(clazz) ?: services
    ?: throw NoSuchElementException("Could not find an implementation for service class ${clazz.qualifiedName}").apply {
        if (suppressed != null) addSuppressed(suppressed!!)
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
    fun fallBackServicesSeq(): Sequence<T> {
        return Services.implementations(Services.qualifiedNameOrFail(clazz)).orEmpty()
            .map { it.value as T }
    }

    fun jdkServices(): Sequence<T> = sequence {
        val current = ServiceLoader.load(clazz.java).iterator()
        if (current.hasNext()) {
            yieldAll(current)
        } else {
            yieldAll(ServiceLoader.load(clazz.java, clazz.java.classLoader))
        }
    }

    fun bothServices(): Sequence<T> = sequence {
        Services.getOverrideOrNull(clazz)?.let { yield(it) }

        var jdkServices = ServiceLoader.load(clazz.java).toList()
        if (jdkServices.isEmpty()) {
            jdkServices = ServiceLoader.load(clazz.java, clazz.java.classLoader).toList()
        }
        yieldAll(jdkServices)

        Services.implementationsDirectly(Services.qualifiedNameOrFail(clazz)).asSequence()
            .filter { impl ->
                // Drop duplicated
                jdkServices.none { it.javaClass.name == impl.implementationClass }
            }
            .forEach { yield(it.instance.value as T) }
    }




    return when (loaderType) {
        LoaderType.JDK -> jdkServices()
        LoaderType.BOTH -> bothServices()
        LoaderType.FALLBACK -> fallBackServicesSeq()
    }
}