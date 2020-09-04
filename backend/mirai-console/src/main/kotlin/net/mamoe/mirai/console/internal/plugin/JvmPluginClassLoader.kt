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

import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap

internal class JvmPluginClassLoader(
    urls: Array<URL>,
    parent: ClassLoader?,
    val classloaders: Collection<JvmPluginClassLoader>
) : URLClassLoader(urls, parent) {
    private val cache = ConcurrentHashMap<String, Class<*>>()

    companion object {
        init {
            ClassLoader.registerAsParallelCapable()
        }
    }

    // private val

    override fun findClass(name: String): Class<*> {
        return findClass(name, false) ?: throw ClassNotFoundException(name)
    }

    internal fun findClass(name: String, disableGlobal: Boolean): Class<*>? {
        val cachedClass = cache[name]
        if (cachedClass != null) return cachedClass
        kotlin.runCatching { return super.findClass(name).also { cache[name] = it } }
        if (disableGlobal)
            return null
        classloaders.forEach { otherClassloader ->
            if (otherClassloader === this) return@forEach
            val otherClass = otherClassloader.findClass(name, true)
            if (otherClass != null) return otherClass
        }
        return null
    }
}