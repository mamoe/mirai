/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.internal.plugin

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

internal class JvmPluginClassLoader(
    file: File,
    parent: ClassLoader?,
) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    //// 只允许插件 getResource 时获取插件自身资源, #205
    override fun getResources(name: String?): Enumeration<URL> = findResources(name)
    override fun getResource(name: String?): URL? = findResource(name)
    // getResourceAsStream 在 URLClassLoader 中通过 getResource 确定资源
    //      因此无需 override getResourceAsStream
}
