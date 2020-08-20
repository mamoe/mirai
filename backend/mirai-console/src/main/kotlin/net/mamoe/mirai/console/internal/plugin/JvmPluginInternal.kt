/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.util.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal val <T> T.job: Job where T : CoroutineScope, T : Plugin get() = this.coroutineContext[Job]!!

/**
 * Hides implementations from [JvmPlugin]
 */
@PublishedApi
internal abstract class JvmPluginInternal(
    parentCoroutineContext: CoroutineContext
) : JvmPlugin,
    CoroutineScope {

    private val resourceContainerDelegate by lazy { this::class.java.classLoader.asResourceContainer() }
    override fun getResourceAsStream(name: String): InputStream? = resourceContainerDelegate.getResourceAsStream(name)

    // region JvmPlugin
    /**
     * Initialized immediately after construction of [JvmPluginInternal] instance
     */
    @Suppress("PropertyName")
    internal open lateinit var _description: JvmPluginDescription

    final override val description: JvmPluginDescription get() = _description

    final override val logger: MiraiLogger by lazy {
        MiraiConsole.newLogger(
            this._description.name
        )
    }

    private var firstRun = true

    final override val dataFolder: File by lazy {
        File(
            PluginManager.pluginsDataFolder,
            description.name
        ).apply { mkdir() }
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
    internal var _intrinsicCoroutineContext: CoroutineContext =
        EmptyCoroutineContext

    @JvmField
    internal val coroutineContextInitializer = {
        CoroutineExceptionHandler { _, throwable -> logger.error(throwable) }
            .plus(parentCoroutineContext)
            .plus(SupervisorJob(parentCoroutineContext[Job])) + _intrinsicCoroutineContext
    }

    private fun refreshCoroutineContext(): CoroutineContext {
        return coroutineContextInitializer().also { _coroutineContext = it }
    }

    private val contextUpdateLock: ReentrantLock =
        ReentrantLock()
    private var _coroutineContext: CoroutineContext? = null
    final override val coroutineContext: CoroutineContext
        get() = _coroutineContext
            ?: contextUpdateLock.withLock { _coroutineContext ?: refreshCoroutineContext() }

    // endregion
}

internal inline fun AtomicLong.updateWhen(condition: (Long) -> Boolean, update: (Long) -> Long): Boolean {
    while (true) {
        val current = value
        if (condition(current)) {
            if (compareAndSet(0, update(current))) {
                return true
            } else continue
        }
        return false
    }
}