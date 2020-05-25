/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.builtin

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.setting.*
import net.mamoe.mirai.console.utils.JavaPluginScheduler
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty


/**
 * Java 或 Kotlin Jar 插件
 *
 * @see JavaPlugin Java 插件
 * @see KotlinPlugin Kotlin 插件
 */
interface JvmPlugin : Plugin, CoroutineScope {
    /** 日志 */
    val logger: MiraiLogger

    /** 插件描述 */
    val description: JvmPluginDescription

    /** 所属插件加载器实例 */
    override val loader: JarPluginLoader get() = JarPluginLoader

    @JvmDefault
    fun onLoad() {
    }

    @JvmDefault
    fun onEnable() {
    }

    @JvmDefault
    fun onDisable() {
    }
}

/**
 * Java 插件的父类
 */
abstract class JavaPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, JvmPluginImpl(parentCoroutineContext) {

    /**
     * Java API Scheduler
     */
    val scheduler: JavaPluginScheduler =
        JavaPluginScheduler(this.coroutineContext)
}

abstract class KotlinPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, JvmPluginImpl(parentCoroutineContext) {

    abstract inner class PluginSetting : Setting() {
        private val track =
            @Suppress("LeakingThis")
            loader.settingStorage.trackOn(this)

        init {
            this@KotlinPlugin.job.invokeOnCompletion {
                track.close()
            }
        }

        override fun onElementChanged(value: Value<*>) {
            TODO()
        }
    }
}

internal val <T> T.job: Job where T : CoroutineScope, T : Plugin get() = this.coroutineContext[Job]!!

internal sealed class JvmPluginImpl(
    parentCoroutineContext: CoroutineContext
) : JvmPlugin, CoroutineScope {
    // region JvmPlugin
    /**
     * Initialized immediately after construction of [JvmPluginImpl] instance
     */
    @Suppress("PropertyName")
    internal lateinit var _description: JvmPluginDescription

    override val description: JvmPluginDescription get() = _description

    final override val logger: MiraiLogger by lazy { MiraiConsole.newLogger(this._description.name) }

    private var firstRun = true

    override val dataFolder: File by lazy {
        File(PluginManager.pluginsDataFolder, description.name).apply { mkdir() }
    }

    internal fun internalOnDisable() {
        firstRun = false
        this.onDisable()
    }

    internal fun internalOnLoad() {
        this.onLoad()
    }

    internal fun internalOnEnable() {
        if (!firstRun) refreshCoroutineContext()
        this.onEnable()
    }

    // endregion

    // region CoroutineScope

    // for future use
    @Suppress("PropertyName")
    @JvmField
    internal var _intrinsicCoroutineContext: CoroutineContext = EmptyCoroutineContext

    @JvmField
    internal val coroutineContextInitializer = {
        CoroutineExceptionHandler { _, throwable -> logger.error(throwable) }
            .plus(parentCoroutineContext)
            .plus(SupervisorJob(parentCoroutineContext[Job])) + _intrinsicCoroutineContext
    }

    private fun refreshCoroutineContext(): CoroutineContext {
        return coroutineContextInitializer().also { _coroutineContext = it }
    }

    private val contextUpdateLock: ReentrantLock = ReentrantLock()
    private var _coroutineContext: CoroutineContext? = null
    final override val coroutineContext: CoroutineContext
        get() = _coroutineContext
            ?: contextUpdateLock.withLock { _coroutineContext ?: refreshCoroutineContext() }

    // endregion
}