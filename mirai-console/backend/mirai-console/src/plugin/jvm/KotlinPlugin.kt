/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "RedundantVisibilityModifier")

package net.mamoe.mirai.console.plugin.jvm

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Kotlin 插件的父类.
 */
public abstract class KotlinPlugin : JvmPlugin, AbstractJvmPlugin {
    /**
     * 通过一个指定的 [JvmPluginDescription] 构造插件示例
     *
     * 当使用 `plugin.yml` 加载插件示例时不能使用此构造器
     */
    @JvmOverloads
    public constructor(
        description: JvmPluginDescription,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    ) : super(description, parentCoroutineContext)


    /**
     * 通过插件内置的 `plugin.yml` 构造插件实例
     *
     * @since 2.16.0
     */
    @JvmOverloads
    public constructor(
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    ) : super(parentCoroutineContext)


    init {
        __jpi_try_to_init_dependencies()
    }
}

/*

public object MyPlugin : KotlinPlugin()

public object AccountPluginData : PluginData by MyPlugin.getPluginData() {
    public val s by value(1)
}
*/