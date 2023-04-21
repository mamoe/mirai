/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ServicesKt_common")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

public object Services {
    private val lock = reentrantLock()
    public fun <T : Any> qualifiedNameOrFail(clazz: KClass<out T>): String =
        clazz.qualifiedName ?: error("Could not find qualifiedName for $clazz")

    internal class Implementation(
        val implementationClass: String,
        val instance: Lazy<Any>
    )

    private val registered: MutableMap<String, MutableList<Implementation>> = mutableMapOf()
    private val overrided: MutableMap<String, Implementation> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getOverrideOrNull(clazz: KClass<out T>): T? {
        lock.withLock {
            return overrided[qualifiedNameOrFail(clazz)]?.instance?.value as T?
        }
    }

    public fun registerAsOverride(baseClass: String, implementationClass: String, implementation: () -> Any) {
        lock.withLock {
            overrided[baseClass] = Implementation(implementationClass, lazy(implementation))
        }
    }

    public fun register(baseClass: String, implementationClass: String, implementation: () -> Any) {
        lock.withLock {
            registered.getOrPut(baseClass, ::mutableListOf)
                .add(Implementation(implementationClass, lazy(implementation)))
        }
    }

    public fun firstImplementationOrNull(baseClass: String): Any? {
        lock.withLock {
            overrided[baseClass]?.let { return it.instance.value }
            return registered[baseClass]?.firstOrNull()?.instance?.value
        }
    }

    public fun implementations(baseClass: String): Sequence<Lazy<Any>>? {
        lock.withLock {
            val implementations = registered[baseClass]
            val forced = overrided[baseClass]
            if (forced == null && implementations == null) return null

            val implementationsSnapshot = implementations?.toList().orEmpty()

            return sequence {
                if (forced != null) yield(forced.instance)

                implementationsSnapshot.forEach { yield(it.instance) }
            }
        }
    }

    internal fun implementationsDirectly(baseClass: String) =
        lock.withLock { registered[baseClass]?.toList().orEmpty() }

    public fun print(): String {
        lock.withLock {
            return registered.entries.joinToString { "${it.key}:${it.value}" }
        }
    }
}

public expect fun <T : Any> loadServiceOrNull(clazz: KClass<out T>, fallbackImplementation: String? = null): T?
public expect fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String? = null): T
public expect fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T>

public inline fun <reified T : Any> loadService(fallbackImplementation: String? = null): T =
    loadService(T::class, fallbackImplementation)

// do not inline: T will be inferred to returning type of `fallbackImplementation`
public fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: () -> T): T =
    loadServiceOrNull(clazz) ?: fallbackImplementation()