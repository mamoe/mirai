/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "PropertyName", "PrivatePropertyName")

package net.mamoe.mirai.console.data

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.internal.plugin.updateWhen
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.*

/**
 * 链接自动保存的 [PluginData].
 *
 * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
 *
 * 若 [AutoSavePluginDataHolder.coroutineContext] 含有 [Job], 则 [AutoSavePluginData] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
 *
 * @see PluginData
 */
public open class AutoSavePluginData private constructor(
    @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?
) : AbstractPluginData() {
    private lateinit var owner_: AutoSavePluginDataHolder
    private val autoSaveIntervalMillis_: LongRange get() = owner_.autoSaveIntervalMillis
    private lateinit var storage_: PluginDataStorage

    public constructor() : this(null)


    @ConsoleExperimentalAPI
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        check(owner is AutoSavePluginDataHolder) { "owner must be AutoSavePluginDataHolder for AutoSavePluginData" }

        if (this::storage_.isInitialized) {
            check(storage == this.storage_) { "AutoSavePluginData is already initialized with one storage and cannot be reinitialized with another." }
        }

        this.storage_ = storage
        this.owner_ = owner

        owner_.coroutineContext[Job]?.invokeOnCompletion {
            kotlin.runCatching {
                doSave()
            }.onFailure { e ->
                owner_.coroutineContext[CoroutineExceptionHandler]?.handleException(owner_.coroutineContext, e)
                    ?.let { return@invokeOnCompletion }
                MiraiConsole.mainLogger.error(
                    "An exception occurred when saving config ${this@AutoSavePluginData::class.qualifiedNameOrTip} " +
                            "but CoroutineExceptionHandler not found in PluginDataHolder.coroutineContext for ${owner::class.qualifiedNameOrTip}",
                    e
                )
            }
        }

        if (shouldPerformAutoSaveWheneverChanged()) {
            owner_.launch(CoroutineName("AutoSavePluginData.timedAutoSave: ${this::class.qualifiedNameOrTip}")) {
                while (isActive) {
                    try {
                        delay(autoSaveIntervalMillis_.last)  // 定时自动保存一次, 用于 kts 序列化的对象
                    } catch (e: CancellationException) {
                        return@launch
                    }
                    withContext(owner_.coroutineContext) {
                        doSave()
                    }
                }
            }
        }
    }

    @JvmField
    @Volatile
    internal var lastAutoSaveJob_: Job? = null

    @JvmField
    internal val currentFirstStartTime_ = atomic(0L)

    /**
     * @return `true` 时, 一段时间后, 即使无属性改变, 也会进行保存.
     */
    @ConsoleExperimentalAPI
    protected open fun shouldPerformAutoSaveWheneverChanged(): Boolean {
        return true
    }

    private val updaterBlock: suspend CoroutineScope.() -> Unit = l@{
        if (::storage_.isInitialized) {
            currentFirstStartTime_.updateWhen({ it == 0L }, { currentTimeMillis })
            try {
                delay(autoSaveIntervalMillis_.first.coerceAtLeast(1000)) // for safety
            } catch (e: CancellationException) {
                return@l
            }

            if (lastAutoSaveJob_ == this.coroutineContext[Job]) {

                withContext(owner_.coroutineContext) {
                    doSave()
                }
            } else {
                if (currentFirstStartTime_.updateWhen(
                        { currentTimeMillis - it >= autoSaveIntervalMillis_.last },
                        { 0 })
                ) {
                    withContext(owner_.coroutineContext) {
                        doSave()
                    }
                }
            }
        }
    }

    public final override fun onValueChanged(value: Value<*>) {
        debuggingLogger1.error { "onValueChanged: $value" }
        if (::owner_.isInitialized) {
            lastAutoSaveJob_ = owner_.launch(
                block = updaterBlock,
                context = CoroutineName("AutoSavePluginData.passiveAutoSave: ${this::class.qualifiedNameOrTip}")
            )
        }
    }

    private fun doSave() {
        debuggingLogger1.error { "doSave: ${this::class.qualifiedName}" }
        storage_.store(owner_, this)
    }
}

internal val debuggingLogger1 by lazy {
    DefaultLogger("debug").withSwitch(false)
}

@Suppress("RESULT_CLASS_IN_RETURN_TYPE")
internal inline fun <R> MiraiLogger.runCatchingLog(message: String? = null, block: () -> R): Result<R> {
    return kotlin.runCatching {
        block()
    }.onFailure {
        if (message != null) {
            error(message, it)
        } else error(it)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
internal inline fun <R> MiraiLogger.runCatchingLog(message: (Throwable) -> String, block: () -> R): R? {
    return kotlin.runCatching {
        block()
    }.onFailure {
        error(message(it), it)
    }.getOrNull()
}