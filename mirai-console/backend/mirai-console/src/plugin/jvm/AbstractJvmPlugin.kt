/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
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

    public final override fun permissionId(name: String): PermissionId =
        PermissionService.INSTANCE.allocatePermissionIdForPlugin(this, name)

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

    /**
     * 立即保存 [PluginData]
     *
     * @see reloadPluginData
     * @since 2.9
     */
    @JvmName("savePluginData")
    public fun <T : PluginData> T.save(): Unit = loader.dataStorage.store(this@AbstractJvmPlugin, this)

    /**
     * 立即保存 [PluginConfig]
     *
     * @see reloadPluginConfig
     * @since 2.9
     */
    @JvmName("savePluginConfig")
    public fun <T : PluginConfig> T.save(): Unit = loader.configStorage.store(this@AbstractJvmPlugin, this)

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

/**
 * 立即保存 [PluginData]
 *
 * @see AbstractJvmPlugin.save
 * @since 2.9
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.savePluginData(instance: PluginData): Unit = this.run { instance.save() }

/**
 * 立即保存 [PluginConfig]
 *
 * @see AbstractJvmPlugin.save
 * @since 2.9
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.savePluginConfig(instance: PluginConfig): Unit = this.run { instance.save() }
