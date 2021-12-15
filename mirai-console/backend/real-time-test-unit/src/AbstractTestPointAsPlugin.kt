/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.rttu

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

public abstract class AbstractTestPointAsPlugin {
    protected abstract fun newPluginDescription(): JvmPluginDescription

    protected open fun KotlinPlugin.onInit() {}
    protected open fun KotlinPlugin.onLoad0(storage: PluginComponentStorage) {}
    protected open fun KotlinPlugin.onEnable0() {}
    protected open fun KotlinPlugin.onDisable0() {}

    internal open fun onConsoleStartSuccessfully() {}
    internal open fun beforeConsoleStartup() {}


    @Suppress("unused")
    @PublishedApi
    internal abstract class TestPointPluginImpl(
        private val impl: AbstractTestPointAsPlugin
    ) : KotlinPlugin(impl.newPluginDescription()) {

        init {
            impl.apply { onInit() }
        }

        @PublishedApi
        internal constructor(
            impl: Class<out AbstractTestPointAsPlugin>
        ) : this(impl.kotlin.objectInstance ?: impl.newInstance())

        override fun onDisable() {
            impl.apply { onDisable0() }
        }

        override fun onEnable() {
            impl.apply { onEnable0() }
        }

        override fun PluginComponentStorage.onLoad() {
            impl.apply { onLoad0(this@onLoad) }
        }
    }

}

