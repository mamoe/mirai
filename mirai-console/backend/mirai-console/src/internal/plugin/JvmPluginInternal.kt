/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.runCatchingLog
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.safeCast
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.*
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

    final override val parentPermission: Permission by lazy {
        PermissionService.INSTANCE.register(
            PermissionService.INSTANCE.allocatePermissionIdForPlugin(this, "*"),
            "The base permission"
        )
    }

    final override var isEnabled: Boolean = false
        internal set

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
        PluginManager.pluginsDataPath.resolve(description.id).apply { mkdir() }
    }

    final override val dataFolder: File by lazy {
        dataFolderPath.toFile()
    }

    final override val configFolderPath: Path by lazy {
        PluginManager.pluginsConfigPath.resolve(description.id).apply { mkdir() }
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
            onFailure = { err ->
                cancel(CancellationException("Exception while disabling plugin", err))

                // @TestOnly
                if (err is ConsoleJvmPluginTestFailedError) throw err

                if (MiraiConsoleImplementation.getInstance().consoleLaunchOptions.crashWhenPluginLoadFailed) {
                    throw err
                }
            }
        )
        isEnabled = false
    }

    @Throws(Throwable::class)
    internal fun internalOnLoad() {
        val componentStorage = PluginComponentStorage(this)
        onLoad(componentStorage)
        GlobalComponentStorage.mergeWith(componentStorage)
    }

    internal fun internalOnEnable(): Boolean {
        parentPermission
        if (!firstRun) refreshCoroutineContext()

        val except = javaClass.getDeclaredAnnotation(ConsoleJvmPluginFuncCallbackStatusExcept.OnEnable::class.java)
        kotlin.runCatching {
            onEnable()
        }.fold(
            onSuccess = {
                if (except?.excepted == ConsoleJvmPluginFuncCallbackStatus.FAILED) {
                    val msg = "Test point '${javaClass.name}' assets failed but onEnable() invoked successfully"
                    cancel(msg)
                    logger.error(msg)
                    throw AssertionError(msg)
                }
                isEnabled = true
                return true
            },
            onFailure = { err ->
                cancel(CancellationException("Exception while enabling plugin", err))
                logger.error(err)

                // @TestOnly
                if (err is ConsoleJvmPluginTestFailedError) throw err

                when (except?.excepted) {
                    ConsoleJvmPluginFuncCallbackStatus.SUCCESS -> throw err
                    ConsoleJvmPluginFuncCallbackStatus.FAILED -> return false
                    else -> {}
                }

                if (MiraiConsoleImplementation.getInstance().consoleLaunchOptions.crashWhenPluginLoadFailed) {
                    throw err
                }
                return false
            }
        )
    }

    // region JvmPlugin - Single Module Dependencies
    @Suppress("FunctionName")
    @JvmSynthetic
    internal fun __jpi_try_to_init_dependencies() {
        val classloader = javaClass.classLoader.safeCast<JvmPluginClassLoaderN>() ?: return
        val desc = try {
            Objects.requireNonNull(description)
        } catch (ignored: NullPointerException) {
            return
        }
        if (desc.dependencies.isEmpty()) {
            classloader.linkPluginLibraries(logger)
        }

    }
    // endregion

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
            .plus(CoroutineName("Plugin ${(this as AbstractJvmPlugin).dataHolderName}"))
            .plus(
                SupervisorJob(parentCoroutineContext[Job] ?: JvmPluginLoader.coroutineContext[Job]!!)
            )
            .also {
                if (!MiraiConsole.isActive) return@also
                JvmPluginLoader.coroutineContext[Job]!!.invokeOnCompletion {
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
            if (compareAndSet(current, update(current))) {
                return true
            } else continue
        }
        return false
    }
}

internal val Throwable.rootCauseOrSelf: Throwable get() = generateSequence(this) { it.cause }.lastOrNull() ?: this