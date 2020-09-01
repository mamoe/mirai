/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.jvm

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Java 插件的父类
 */
public abstract class JavaPlugin @JvmOverloads constructor(
    public final override val description: JvmPluginDescription,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, AbstractJvmPlugin(parentCoroutineContext) {

    /**
     * Java API Scheduler
     */
    public val scheduler: JavaPluginScheduler = JavaPluginScheduler(this.coroutineContext)
}