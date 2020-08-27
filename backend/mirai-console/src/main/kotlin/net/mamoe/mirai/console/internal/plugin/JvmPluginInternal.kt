/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import java.nio.file.Path
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

    final override var isEnabled: Boolean = false

    private val resourceContainerDelegate by lazy { this::class.java.classLoader.asResourceContainer() }
    override fun getResourceAsStream(path: String): InputStream? = resourceContainerDelegate.getResourceAsStream(path)

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

    final override val dataFolderPath: Path by lazy {
        PluginManager.pluginsDataPath.resolve(description.name).apply { mkdir() }
    }

    final override val dataFolder: File by lazy {
        dataFolderPath.toFile()
    }

    override val configFolderPath: Path by lazy {
        PluginManager.pluginsConfigPath.resolve(description.name).apply { mkdir() }
    }

    override val configFolder: File by lazy {
        configFolderPath.toFile()
    }

    internal fun internalOnDisable() {
        firstRun = false
        kotlin.runCatching {
            onDisable()
        }.fold(
            onSuccess = {
                cancel(CancellationException("plugin disabled"))
            },
            onFailure = {
                cancel(CancellationException("Exception while enabling plugin", it))
            }
        )
        isEnabled = false
    }

    @Throws(Throwable::class)
    internal fun internalOnLoad() { // propagate exceptions
        onLoad()
    }

    internal fun internalOnEnable(): Boolean {
        if (!firstRun) refreshCoroutineContext()
        kotlin.runCatching {
            onEnable()
        }.fold(
            onSuccess = {
                isEnabled = true
                return true
            },
            onFailure = {
                cancel(CancellationException("Exception while enabling plugin", it))
                return false
            }
        )
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
            .plus(SupervisorJob(parentCoroutineContext[Job]))
            .also {
                JarPluginLoaderImpl.coroutineContext[Job]!!.invokeOnCompletion {
                    this.cancel()
                }
            }
            .plus(_intrinsicCoroutineContext)
    }

    private fun refreshCoroutineContext(): CoroutineContext {
        return coroutineContextInitializer().also { _coroutineContext = it }.also {
            job.invokeOnCompletion { e ->
                if (e != null) {
                    logger.error(e)
                    if (this.isEnabled) safeLoader.disable(this)
                }
            }
        }
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