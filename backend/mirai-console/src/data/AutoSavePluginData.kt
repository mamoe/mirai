/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "PropertyName", "PrivatePropertyName")

package net.mamoe.mirai.console.data

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.data.qualifiedNameOrTip
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.TimedTask
import net.mamoe.mirai.console.util.launchTimedTask
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
    // KEEP THIS PRIMARY CONSTRUCTOR FOR FUTURE USE: WE'LL SUPPORT SERIALIZERS_MODULE FOR POLYMORPHISM
    @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?,
) : AbstractPluginData() {
    private lateinit var owner_: AutoSavePluginDataHolder
    private val autoSaveIntervalMillis_: LongRange get() = owner_.autoSaveIntervalMillis
    private lateinit var storage_: PluginDataStorage

    public final override val saveName: String
        get() = _saveName

    @Suppress("JoinDeclarationAndAssignment") // bug
    private lateinit var _saveName: String

    public constructor(saveName: String) : this(null) {
        _saveName = saveName
    }

    private fun logException(e: Throwable) {
        owner_.coroutineContext[CoroutineExceptionHandler]?.handleException(owner_.coroutineContext, e)
            ?.let { return }
        MiraiConsole.mainLogger.error(
            "An exception occurred when saving config ${this@AutoSavePluginData::class.qualifiedNameOrTip} " +
                "but CoroutineExceptionHandler not found in PluginDataHolder.coroutineContext for ${owner_::class.qualifiedNameOrTip}",
            e
        )
    }

    @ConsoleExperimentalApi
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        check(owner is AutoSavePluginDataHolder) { "owner must be AutoSavePluginDataHolder for AutoSavePluginData" }

        if (this::storage_.isInitialized) {
            check(storage == this.storage_) { "AutoSavePluginData is already initialized with one storage and cannot be reinitialized with another." }
        }

        this.storage_ = storage
        this.owner_ = owner

        owner_.coroutineContext[Job]?.invokeOnCompletion { save() }

        saverTask = owner_.launchTimedTask(
            intervalMillis = autoSaveIntervalMillis_.first,
            coroutineContext = CoroutineName("AutoSavePluginData.saver: ${this::class.qualifiedNameOrTip}")
        ) { save() }

        if (shouldPerformAutoSaveWheneverChanged()) {
            // 定时自动保存, 用于 kts 序列化的对象
            owner_.launch(CoroutineName("AutoSavePluginData.timedAutoSave: ${this::class.qualifiedNameOrTip}")) {
                while (isActive) {
                    runIgnoreException<CancellationException> { delay(autoSaveIntervalMillis_.last) } ?: return@launch
                    doSave()
                }
            }
        }
    }

    private var saverTask: TimedTask? = null

    /**
     * @return `true` 时, 一段时间后, 即使无属性改变, 也会进行保存.
     */
    @ConsoleExperimentalApi
    protected open fun shouldPerformAutoSaveWheneverChanged(): Boolean {
        return true
    }

    @ConsoleExperimentalApi
    public final override fun onValueChanged(value: Value<*>) {
        debuggingLogger1.error { "onValueChanged: $value" }
        saverTask?.setChanged()
    }

    private fun save() {
        kotlin.runCatching {
            doSave()
        }.onFailure { e ->
            logException(e)
        }
    }

    private fun doSave() {
        debuggingLogger1.error { "doSave: ${this::class.qualifiedName}" }
        storage_.store(owner_, this)
    }
}

internal val debuggingLogger1 by lazy {
    MiraiLogger.create("console.debug").withSwitch(false)
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

@Suppress("SpellCheckingInspection")
private const val MAGIC_NUMBER_CFST_INIT: Long = Long.MAX_VALUE
