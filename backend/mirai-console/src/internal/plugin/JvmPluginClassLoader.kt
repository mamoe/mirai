/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.plugin.jvm.ExportManager
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class JvmPluginClassLoader(
    val file: File,
    parent: ClassLoader?,
    val classLoaders: Collection<JvmPluginClassLoader>,
) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    //// 只允许插件 getResource 时获取插件自身资源, #205
    override fun getResources(name: String?): Enumeration<URL> = findResources(name)
    override fun getResource(name: String?): URL? = findResource(name)
    // getResourceAsStream 在 URLClassLoader 中通过 getResource 确定资源
    //      因此无需 override getResourceAsStream

    override fun toString(): String {
        return "JvmPluginClassLoader{source=$file}"
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
        // First. Try direct load in cache.
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
        if (disableGlobal) {
            // ==== Process Loading Request From JvmPluginClassLoader ====
            //
            // If load from other classloader,
            // means no other loaders are cached.
            // direct load
            return kotlin.runCatching {
                super.findClass(name).also { cache[name] = it }
            }.getOrElse {
                if (it is ClassNotFoundException) null
                else throw it
            }?.also {
                // This request is from other classloader,
                // so we need to check the class is exported or not.
                val filter = declaredFilter
                if (filter != null && !filter.isExported(name)) {
                    throw LoadingDeniedException(name)
                }
            }
        }

        // ==== Process Loading Request From JDK ClassLoading System ====

        // First. scan other classLoaders's caches
        classLoaders.forEach { otherClassloader ->
            if (otherClassloader === this) return@forEach
            val filter = otherClassloader.declaredFilter
            if (otherClassloader.cache.containsKey(name)) {
                return if (filter == null || filter.isExported(name)) {
                    otherClassloader.cache[name]
                } else throw LoadingDeniedException("$name was not exported by $otherClassloader")
            }
        }
        classLoaders.forEach { otherClassloader ->
            val other = kotlin.runCatching {
                if (otherClassloader === this) super.findClass(name).also { cache[name] = it }
                else otherClassloader.findClass(name, true)
            }.onFailure { err ->
                if (err is LoadingDeniedException || err !is ClassNotFoundException)
                    throw err
            }.getOrNull()
            if (other != null) return other
        }
        throw ClassNotFoundException(name)
    }
}

internal class LoadingDeniedException(name: String) : ClassNotFoundException(name)
