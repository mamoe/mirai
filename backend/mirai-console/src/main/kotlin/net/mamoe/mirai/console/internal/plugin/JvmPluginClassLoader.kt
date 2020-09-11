/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.plugin.jvm.ExportManager
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap

internal class LoadingDeniedException(name: String) : ClassNotFoundException(name)

internal class JvmPluginClassLoader(
    val source: Any,
    urls: Array<URL>,
    parent: ClassLoader?,
    val classLoaders: Collection<JvmPluginClassLoader>
) : URLClassLoader(urls, parent) {
    override fun toString(): String {
        return "JvmPluginClassLoader{source=$source}"
    }

    private val cache = ConcurrentHashMap<String, Class<*>>()
    internal var declaredFilter: ExportManager? = null

    companion object {
        val loadingLock = ConcurrentHashMap<String, Any>()

        init {
            ClassLoader.registerAsParallelCapable()
        }
    }

    override fun findClass(name: String): Class<*> {
        synchronized(kotlin.run {
            val lock = Any()
            loadingLock.putIfAbsent(name, lock) ?: lock
        }) {
            return findClass(name, false) ?: throw ClassNotFoundException(name)
        }
    }

    internal fun findClass(name: String, disableGlobal: Boolean): Class<*>? {
        val cachedClass = cache[name]
        if (cachedClass != null) {
            if (disableGlobal) {
                val filter = declaredFilter
                if (filter != null && !filter.isExported(name)) {
                    throw LoadingDeniedException(name)
                }
            }
            return cachedClass
        }
        if (disableGlobal)
            return kotlin.runCatching {
                super.findClass(name).also { cache[name] = it }
            }.getOrElse {
                if (it is ClassNotFoundException) null
                else throw it
            }?.also {
                val filter = declaredFilter
                if (filter != null && !filter.isExported(name)) {
                    throw LoadingDeniedException(name)
                }
            }

        classLoaders.forEach { otherClassloader ->
            if (otherClassloader === this) return@forEach
            val filter = otherClassloader.declaredFilter
            if (otherClassloader.cache.containsKey(name)) {
                return if (filter == null || filter.isExported(name)) {
                    otherClassloader.cache[name]
                } else throw LoadingDeniedException("$name was not exported by $otherClassloader")
            }
        }

        return kotlin.runCatching { super.findClass(name).also { cache[name] = it } }.getOrElse {
            if (it is ClassNotFoundException) {
                classLoaders.forEach { otherClassloader ->
                    if (otherClassloader === this) return@forEach
                    val other = kotlin.runCatching {
                        otherClassloader.findClass(name, true)
                    }.getOrElse { err ->
                        if (err is LoadingDeniedException || err !is ClassNotFoundException) throw it
                        null
                    }
                    if (other != null) return other
                }
            }
            throw it
        }
    }
}
