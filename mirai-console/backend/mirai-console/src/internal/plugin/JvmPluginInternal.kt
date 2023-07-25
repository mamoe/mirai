/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(MiraiInternalApi::class, ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal.plugin

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.runCatchingLog
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.shutdown.ShutdownDaemon
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.ResourceContainer.Companion.asResourceContainer
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
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
    internal enum class PluginStatus {
        ALLOCATED,

        CRASHED_LOAD_ERROR(Flags.ALLOW_SWITCH_TO_DISABLE),
        CRASHED_ENABLE_ERROR(Flags.ALLOW_SWITCH_TO_DISABLE),
        CRASHED_DISABLE_ERROR,

        LOAD_PENDING,
        LOAD_LOADING,
        LOAD_LOAD_DONE,

        ENABLE_PENDING,
        ENABLE_ENABLING,
        ENABLED(Flags.ALLOW_SWITCH_TO_DISABLE),

        DISABLE_PENDING,
        DISABLE_DISABLING,
        DISABLED,

        ;

        private val flags: Int

        constructor() : this(0)
        constructor(flags: Int) {
            this.flags = flags
        }

        internal object Flags { // compiler bug: [UNINITIALIZED_VARIABLE] Variable 'FLAG_ALLOW_SWITCH_TO_DISABLE' must be initialized
            internal const val ALLOW_SWITCH_TO_DISABLE = 1 shl 0
        }

        fun hasFlag(flag: Int): Boolean = flags.and(flag) != 0
    }

    private val pluginStatus = atomic(PluginStatus.ALLOCATED)

    @get:JvmSynthetic
    internal val currentPluginStatus: PluginStatus get() = pluginStatus.value

    final override val isEnabled: Boolean
        get() = pluginStatus.value === PluginStatus.ENABLED

    @JvmSynthetic
    internal fun switchStatusOrFail(expectFlag: Int, update: PluginStatus) {
        val nowStatus = pluginStatus.value
        if (nowStatus.hasFlag(expectFlag)) {
            if (pluginStatus.compareAndSet(expect = nowStatus, update = update)) {
                return
            }
            error("Failed to switch plugin '$id' status from $nowStatus to $update, current status = ${pluginStatus.value}")
        }
        error(
            "Failed to switch plugin '$id' status to $update because current status $nowStatus doesn't contain flag ${
                Integer.toBinaryString(
                    expectFlag
                )
            }"
        )
    }

    @JvmSynthetic
    internal fun switchStatusOrFail(expect: PluginStatus, update: PluginStatus) {
        val nowStatus = pluginStatus.value
        if (nowStatus === expect) {
            if (pluginStatus.compareAndSet(expect = expect, update = update)) {
                return
            }
            error("Failed to switch plugin '$id' status from $expect to $update, current status=${pluginStatus.value}")
        }
        error("Failed to switch plugin '$id' status from $expect to $update, current status = $nowStatus")
    }


    final override val parentPermission: Permission by lazy {
        PermissionService.INSTANCE.register(
            PermissionService.INSTANCE.allocatePermissionIdForPlugin(this, "*"),
            "The base permission"
        )
    }


    private val resourceContainerDelegate by lazy { this::class.java.classLoader.asResourceContainer() }
    final override fun getResourceAsStream(path: String): InputStream? =
        resourceContainerDelegate.getResourceAsStream(path)

    // region JvmPlugin
    final override val logger: MiraiLogger by lazy {
        BuiltInJvmPluginLoaderImpl.logger.runCatchingLog {
            MiraiLogger.Factory.create(this@JvmPluginInternal::class, this.description.name)
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

        switchStatusOrFail(
            expectFlag = PluginStatus.Flags.ALLOW_SWITCH_TO_DISABLE,
            update = PluginStatus.DISABLE_PENDING,
        )

        firstRun = false
        kotlin.runCatching {
            val crtThread = Thread.currentThread()
            ShutdownDaemon.pluginDisablingThreads.add(crtThread)
            try {
                pluginStatus.value = PluginStatus.DISABLE_DISABLING
                onDisable()
            } finally {
                ShutdownDaemon.pluginDisablingThreads.remove(crtThread)
            }
        }.fold(
            onSuccess = {
                pluginStatus.value = PluginStatus.DISABLED

                cancel(CancellationException("plugin disabled"))
            },
            onFailure = { err ->
                pluginStatus.value = PluginStatus.CRASHED_DISABLE_ERROR

                cancel(CancellationException("Exception while disabling plugin", err))

                // @TestOnly
                if (err is ConsoleJvmPluginTestFailedError) throw err

                if (MiraiConsoleImplementation.getInstance().consoleLaunchOptions.crashWhenPluginLoadFailed) {
                    throw err
                }
            }
        )
    }

    @Throws(Throwable::class)
    internal fun internalOnLoad() {
        switchStatusOrFail(PluginStatus.ALLOCATED, PluginStatus.LOAD_PENDING)

        try {
            pluginStatus.value = PluginStatus.LOAD_LOADING

            val componentStorage = PluginComponentStorage(this)
            onLoad(componentStorage)

            pluginStatus.value = PluginStatus.LOAD_LOAD_DONE
            GlobalComponentStorage.mergeWith(componentStorage)

        } catch (e: Throwable) {
            pluginStatus.value = PluginStatus.CRASHED_LOAD_ERROR

            cancel(CancellationException("Exception while loading plugin", e))

            throw e
        }
    }


    internal fun internalOnEnable(): Boolean {
        switchStatusOrFail(PluginStatus.LOAD_LOAD_DONE, PluginStatus.ENABLE_PENDING)

        parentPermission
        if (!firstRun) refreshCoroutineContext()

        val except = try {
            javaClass.getDeclaredAnnotation(ConsoleJvmPluginFuncCallbackStatusExcept.OnEnable::class.java)
        } catch (e: Throwable) {
            null
        }

        kotlin.runCatching {
            pluginStatus.value = PluginStatus.ENABLE_ENABLING
            onEnable()
        }.fold(
            onSuccess = {
                if (except?.excepted == ConsoleJvmPluginFuncCallbackStatus.FAILED) {
                    val msg = "Test point '${javaClass.name}' assets failed but onEnable() invoked successfully"
                    cancel(msg)
                    logger.error(msg)
                    throw AssertionError(msg)
                }
                pluginStatus.value = PluginStatus.ENABLED
                return true
            },
            onFailure = { err ->
                pluginStatus.value = PluginStatus.CRASHED_ENABLE_ERROR

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
    @get:JvmSynthetic
    internal val _intrinsicCoroutineContext: CoroutineContext by lazy {
        this as AbstractJvmPlugin
        CoroutineName("Plugin $dataHolderName")
    }

    private val pluginParentJob: Job = run {
        val job = parentCoroutineContext[Job] ?: JvmPluginLoader.coroutineContext[Job]!!

        val pluginManagerJob = MiraiConsole.pluginManager.impl.coroutineContext.job

        val allJobs = generateSequence(sequenceOf(pluginManagerJob)) { parentSeqs ->
            parentSeqs.flatMap { it.children }
        }.flatten()

        check(allJobs.contains(job)) {
            "The parent job of plugin `$id' not a child of PluginManager"
        }

        job
    }

    @JvmField
    @JvmSynthetic
    internal val coroutineContextInitializer = {
        CoroutineExceptionHandler { context, throwable ->
            if (throwable.rootCauseOrSelf !is CancellationException) logger.error(
                "Exception in coroutine ${context[CoroutineName]?.name ?: "<unnamed>"} of ${description.name}",
                throwable
            )
        }
            .plus(parentCoroutineContext)
            .plus(SupervisorJob(pluginParentJob))
            .plus(_intrinsicCoroutineContext)
    }

    private fun refreshCoroutineContext(): CoroutineContext {
        return coroutineContextInitializer().also { _coroutineContext = it }.also {
            job.invokeOnCompletion { e ->
                if (e != null) {
                    if (e !is CancellationException) logger.error(e)
                    if (pluginStatus.value == PluginStatus.ENABLED) {
                        safeLoader.disable(this)
                    }
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

internal fun Class<out JvmPluginInternal>.loadPluginDescriptionFromClassLoader(): JvmPluginDescription {
    val classLoader =
        this.classLoader as? JvmPluginClassLoaderN ?: error("Plugin $this is not loaded by JvmPluginClassLoader")

    return classLoader.pluginDescriptionFromPluginResource ?: error("Missing `plugin.yml`")
}

