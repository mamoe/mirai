/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.minutesToMillis
import net.mamoe.mirai.utils.secondsToMillis
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [JavaPlugin] 和 [KotlinPlugin] 的父类. 所有 [JvmPlugin] 都应该拥有此类作为直接或间接父类.
 *
 * @see JavaPlugin
 * @see KotlinPlugin
 */
public abstract class AbstractJvmPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : JvmPlugin, JvmPluginInternal(parentCoroutineContext), AutoSavePluginDataHolder {
    @ConsoleExperimentalApi
    public final override val dataHolderName: String
        get() = this.description.name

    public final override val loader: JvmPluginLoader get() = super<JvmPluginInternal>.loader

    public final override fun permissionId(name: String): PermissionId = PermissionId(description.id, "command.$name")

    /**
     * 重载 [PluginData]
     *
     * @see reloadPluginData
     */
    @JvmName("reloadPluginData")
    public fun <T : PluginData> T.reload(): Unit = loader.dataStorage.load(this@AbstractJvmPlugin, this)

    /**
     * 重载 [PluginConfig]
     *
     * @see reloadPluginConfig
     */
    @JvmName("reloadPluginConfig")
    public fun <T : PluginConfig> T.reload(): Unit = loader.configStorage.load(this@AbstractJvmPlugin, this)

    @ConsoleExperimentalApi
    public override val autoSaveIntervalMillis: LongRange = 30.secondsToMillis..10.minutesToMillis
}

/**
 * 重载一个 [PluginData]
 *
 * @see AbstractJvmPlugin.reload
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.reloadPluginData(instance: PluginData): Unit = this.run { instance.reload() }

/**
 * 重载一个 [PluginConfig]
 *
 * @see AbstractJvmPlugin.reload
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.reloadPluginConfig(instance: PluginConfig): Unit = this.run { instance.reload() }