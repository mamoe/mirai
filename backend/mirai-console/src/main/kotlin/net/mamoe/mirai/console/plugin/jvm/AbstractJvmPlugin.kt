/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.utils.minutesToSeconds
import net.mamoe.mirai.utils.secondsToMillis
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [JavaPlugin] 和 [KotlinPlugin] 的父类
 *
 * @see JavaPlugin
 * @see KotlinPlugin
 */
public abstract class AbstractJvmPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, JvmPluginInternal(parentCoroutineContext) {
    public final override val name: String get() = this.description.name

    public override val autoSaveIntervalMillis: LongRange = 30.secondsToMillis..10.minutesToSeconds
}