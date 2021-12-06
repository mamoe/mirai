/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.plugin.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.reflect.KClass

/**
 * 资源容器.
 *
 * 资源容器可能使用 [Class.getResourceAsStream], [ClassLoader.getResourceAsStream], 也可能使用其他方式, 取决于实现方式.
 *
 * @see JvmPlugin [JvmPlugin] 通过 [ClassLoader.getResourceAsStream] 实现 [ResourceContainer], 使用 [ResourceContainer.asResourceContainer]
 */
public interface ResourceContainer {
    /**
     * 获取一个资源文件.
     *
     * @return 资源文件内容. 在未找到文件时返回 `null`.
     */
    public fun getResourceAsStream(path: String): InputStream?

    /**
     * 读取一个资源文件并以 [Charsets.UTF_8] 解码为 [String].
     *
     * @return 资源文件内容. 在未找到文件时返回 `null`.
     */
    public fun getResource(path: String): String? = getResource(path, Charsets.UTF_8)

    /**
     * 读取一个资源文件并以 [charset] 解码为 [String].
     *
     * @return 资源文件内容. 在未找到文件时返回 `null`.
     */
    public fun getResource(path: String, charset: Charset): String? =
        this.getResourceAsStream(path)?.use(InputStream::readBytes)?.let(::String)

    public companion object {
        /**
         * 使用 [Class.getResourceAsStream] 读取资源文件.
         *
         * @see ClassLoader.asResourceContainer
         */
        @JvmStatic
        @JvmName("create")
        public fun KClass<*>.asResourceContainer(): ResourceContainer = this.java.asResourceContainer()

        /**
         * 使用 [ClassLoader.getResourceAsStream] 读取资源文件.
         */
        @JvmStatic
        @JvmName("create")
        public fun ClassLoader.asResourceContainer(): ResourceContainer = ClassLoaderAsResourceContainer(this)

        /**
         * 使用 [Class.getResourceAsStream] 读取资源文件.
         */
        @JvmStatic
        @JvmName("create")
        public fun Class<*>.asResourceContainer(): ResourceContainer = ClassAsResourceContainer(this)
    }
}

private class ClassAsResourceContainer(
    private val clazz: Class<*>
) : ResourceContainer {
    override fun getResourceAsStream(path: String): InputStream? = clazz.getResourceAsStream(path)
}

private class ClassLoaderAsResourceContainer(
    private val clazz: ClassLoader
) : ResourceContainer {
    override fun getResourceAsStream(path: String): InputStream? = clazz.getResourceAsStream(path)
}