/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.createInstanceOrNull

/**
 * IntegrationTest 测试单元 (Plugin mode)
 *
 * 该单元除了拥有 [AbstractTestPoint] 具有的功能之外, 还可以直接模拟一个插件的行为.
 *
 * 在此单元里, 可以像写正常的 console 插件一样在此写测试时插件
 */
public abstract class AbstractTestPointAsPlugin : AbstractTestPoint() {
    protected abstract fun newPluginDescription(): JvmPluginDescription

    protected open fun KotlinPlugin.onInit() {}
    protected open fun KotlinPlugin.onLoad0(storage: PluginComponentStorage) {}
    protected open fun KotlinPlugin.onEnable0() {}
    protected open fun KotlinPlugin.onDisable0() {}

    protected open fun exceptionHandler(exception: Throwable, step: JvmPluginExecutionStep, instance: KotlinPlugin) {
        IntegrationTestBootstrapContext.failures.add(this.javaClass)
    }

    private fun callEH(exception: Throwable, step: JvmPluginExecutionStep, instance: KotlinPlugin) {
        try {
            exceptionHandler(exception, step, instance)
        } catch (e: Throwable) {
            forceFail(cause = e)
        }
    }

    protected enum class JvmPluginExecutionStep {
        OnEnable, OnDisable, OnLoad
    }


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
        ) : this(
            impl.kotlin.createInstanceOrNull() ?: impl.getConstructor().newInstance()
        )

        override fun onDisable() {
            try {
                impl.apply { onDisable0() }
            } catch (e: Throwable) {
                impl.callEH(e, JvmPluginExecutionStep.OnDisable, this)
                throw e
            }
        }

        override fun onEnable() {
            try {
                impl.apply { onEnable0() }
            } catch (e: Throwable) {
                impl.callEH(e, JvmPluginExecutionStep.OnEnable, this)
                throw e
            }
        }

        override fun PluginComponentStorage.onLoad() {
            try {
                impl.apply { onLoad0(this@onLoad) }
            } catch (e: Throwable) {
                impl.callEH(e, JvmPluginExecutionStep.OnLoad, this@TestPointPluginImpl)
                throw e
            }
        }
    }

}

