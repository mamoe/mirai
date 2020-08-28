/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "RedundantVisibilityModifier")

package net.mamoe.mirai.console.plugin.jvm

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Kotlin 插件的父类.
 *
 * 必须通过 "plugin.yml" 指定主类并由 [JarPluginLoader] 加载.
 */
public abstract class KotlinPlugin @JvmOverloads constructor(
    public final override val description: JvmPluginDescription,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : JvmPlugin, AbstractJvmPlugin(parentCoroutineContext)

/*

public object MyPlugin : KotlinPlugin()

public object AccountPluginData : PluginData by MyPlugin.getPluginData() {
    public val s by value(1)
}
*/