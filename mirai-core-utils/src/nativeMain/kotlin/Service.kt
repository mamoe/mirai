/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.reflect.KClass

public object Services {
    private val lock = reentrantLock()

    private class Implementation(
        val implementationClass: String,
        val instance: Lazy<Any>
    )

    private val registered: MutableMap<String, MutableList<Implementation>> = mutableMapOf()

    public fun register(baseClass: String, implementationClass: String, implementation: () -> Any) {
        lock.withLock {
            registered.getOrPut(baseClass, ::mutableListOf)
                .add(Implementation(implementationClass, lazy(implementation)))
        }
    }

    public fun firstImplementationOrNull(baseClass: String): Any? {
        lock.withLock {
            return registered[baseClass]?.firstOrNull()?.instance?.value
        }
    }

    public fun implementations(baseClass: String): List<Lazy<Any>>? {
        lock.withLock {
            return registered[baseClass]?.map { it.instance }
        }

    }

    public fun print(): String {
        lock.withLock {
            return registered.entries.joinToString { "${it.key}:${it.value}" }
        }
    }
}

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadServiceOrNull(
    clazz: KClass<out T>,
    fallbackImplementation: String?
): T? =
    Services.firstImplementationOrNull(qualifiedNameOrFail(clazz)) as T?

public actual fun <T : Any> loadService(
    clazz: KClass<out T>,
    fallbackImplementation: String?
): T = loadServiceOrNull(clazz, fallbackImplementation)
    ?: error("Could not load service '${clazz.qualifiedName ?: clazz}'. Current services: ${Services.print()}")

public actual fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T> =
    Services.implementations(qualifiedNameOrFail(clazz))?.asSequence()?.map { it.value }.orEmpty().castUp()

private fun <T : Any> qualifiedNameOrFail(clazz: KClass<out T>) =
    clazz.qualifiedName ?: error("Could not find qualifiedName for $clazz")