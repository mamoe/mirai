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

import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.getValue
import net.mamoe.mirai.console.setting.value
import net.mamoe.mirai.console.utils.ConsoleExperimentalAPI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Kotlin 插件的父类.
 *
 * 必须通过 "plugin.yml" 指定主类并由 [JarPluginLoader] 加载.
 */
abstract class KotlinPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, AbstractJvmPlugin(parentCoroutineContext)


/**
 * 在内存动态加载的插件.
 */
@ConsoleExperimentalAPI
abstract class KotlinMemoryPlugin @JvmOverloads constructor(
    description: JvmPluginDescription,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, AbstractJvmPlugin(parentCoroutineContext) {
    final override var _description: JvmPluginDescription
        get() = super._description
        set(value) {
            super._description = value
        }

    init {
        _description = description
    }
}

object MyPlugin : KotlinPlugin()

object AccountSetting : Setting by MyPlugin.getSetting() {
    val s by value(1)
}