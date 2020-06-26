/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.plugin.internal.JvmPluginInternal
import net.mamoe.mirai.console.setting.Setting
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [JavaPlugin] 和 [KotlinPlugin] 的父类
 *
 * @see JavaPlugin
 * @see KotlinPlugin
 */
abstract class AbstractJvmPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, JvmPluginInternal(parentCoroutineContext) {
    final override val name: String get() = this.description.name

    override fun <T : Setting> getSetting(clazz: Class<T>): T = loader.settingStorage.load(this, clazz)
}