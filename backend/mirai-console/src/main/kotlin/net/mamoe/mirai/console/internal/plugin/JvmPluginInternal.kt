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
import net.mamoe.mirai.console.data.runCatchingLog
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.allocatePermissionIdForPlugin
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.NamedSupervisorJob
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

internal val <T> T.job: Job where T : CoroutineScope, T : Plugin get() = this.coroutineContext[Job]!!

/**
 * Hides implementations from [JvmPlugin]
 */
@PublishedApi
internal abstract class JvmPluginInternal(
    parentCoroutineContext: CoroutineContext,
) : JvmPlugin, CoroutineScope {

    @Suppress("LeakingThis")
    internal val componentStorage: PluginComponentStorage = PluginComponentStorage(this)

    final override val parentPermission: Permission by lazy {
        PermissionService.INSTANCE.register(
            PermissionService.INSTANCE.allocatePermissionIdForPlugin(name, "*"),
            "The base permission"
        )
    }

    final override var isEnabled: Boolean = false

    private val resourceContainerDelegate by lazy { this::class.java.classLoader.asResourceContainer() }
    final override fun getResourceAsStream(path: String): InputStream? =
        resourceContainerDelegate.getResourceAsStream(path)

    // region JvmPlugin
    final override val logger: MiraiLogger by lazy {
        BuiltInJvmPluginLoaderImpl.logger.runCatchingLog {
            MiraiConsole.createLogger(this.description.name)
        }.getOrThrow()
    }

    private var firstRun = true

    final override val dataFolderPath: Path by lazy {
        PluginManager.pluginsDataPath.resolve(description.name).apply { mkdir() }
    }

    final override val dataFolder: File by lazy {
        dataFolderPath.toFile()
    }

    final override val configFolderPath: Path by lazy {
        PluginManager.pluginsConfigPath.resolve(description.name).apply { mkdir() }
    }

    final override val configFolder: File by lazy {
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
    internal fun internalOnLoad(componentStorage: PluginComponentStorage) {
        onLoad(componentStorage)
    }

    internal fun internalOnEnable(): Boolean {
        parentPermission
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
                logger.error(it)
                return false
            }
        )
    }

    // endregion

    // region CoroutineScope

    // for future use
    @Suppress("PropertyName")
    internal val _intrinsicCoroutineContext: CoroutineContext by lazy {
        this as AbstractJvmPlugin
        CoroutineName("Plugin $dataHolderName")
    }

    @JvmField
    internal val coroutineContextInitializer = {
        CoroutineExceptionHandler { context, throwable ->
            if (throwable.rootCauseOrSelf !is CancellationException) logger.error(
                "Exception in coroutine ${context[CoroutineName]?.name ?: "<unnamed>"} of ${description.name}",
                throwable
            )
        }
            .plus(parentCoroutineContext)
            .plus(
                NamedSupervisorJob(
                    "Plugin ${(this as AbstractJvmPlugin).dataHolderName}",
                    parentCoroutineContext[Job] ?: BuiltInJvmPluginLoaderImpl.coroutineContext[Job]!!
                )
            )
            .also {
                if (!MiraiConsole.isActive) return@also
                BuiltInJvmPluginLoaderImpl.coroutineContext[Job]!!.invokeOnCompletion {
                    this.cancel()
                }
            }
            .plus(_intrinsicCoroutineContext)
    }

    private fun refreshCoroutineContext(): CoroutineContext {
        return coroutineContextInitializer().also { _coroutineContext = it }.also {
            job.invokeOnCompletion { e ->
                if (e != null) {
                    if (e !is CancellationException) logger.error(e)
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

internal val Throwable.rootCauseOrSelf: Throwable get() = generateSequence(this) { it.cause }.lastOrNull() ?: this