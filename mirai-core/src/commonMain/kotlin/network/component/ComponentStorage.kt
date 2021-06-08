/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import org.jetbrains.annotations.TestOnly

/**
 * Mediator for [component][ComponentKey]s accessing each other.
 *
 * Implementation must be thread-safe.
 *
 * @see MutableComponentStorage
 * @see ConcurrentComponentStorage
 * @see withFallback
 */
internal interface ComponentStorage {
    @get:TestOnly
    val size: Int

    @Throws(NoSuchComponentException::class)
    operator fun <T : Any> get(key: ComponentKey<T>): T
    fun <T : Any> getOrNull(key: ComponentKey<T>): T?

    val keys: Set<ComponentKey<*>>

    override fun toString(): String

    companion object {
        val EMPTY: ComponentStorage by lazy { ConcurrentComponentStorage() }
    }
}

internal fun ComponentStorage?.withFallback(fallback: ComponentStorage?): ComponentStorage {
    if (this == null) return fallback ?: return ComponentStorage.EMPTY
    if (fallback == null) return this
    return CombinedComponentStorage(this, fallback)
}

private class CombinedComponentStorage(
    val main: ComponentStorage,
    val fallback: ComponentStorage,
) : ComponentStorage {
    override val keys: Set<ComponentKey<*>> get() = main.keys + fallback.keys
    override val size: Int get() = main.size + fallback.size

    override fun <T : Any> get(key: ComponentKey<T>): T {
        return main.getOrNull(key) ?: fallback.getOrNull(key) ?: main[key] // let `main` throw exception
    }

    override fun <T : Any> getOrNull(key: ComponentKey<T>): T? {
        return main.getOrNull(key) ?: fallback.getOrNull(key)
    }

    override fun toString(): String = buildString {
        appendLine("CombinedComponentStorage {")
        appendLine("* main:")
        main.toString().lines().forEach {
            append("  ").appendLine(it)
        }
        appendLine("*** fallback:")
        fallback.toString().lines().forEach {
            append("  ").appendLine(it)
        }
        appendLine("}")
    }
}