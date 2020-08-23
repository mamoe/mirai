package net.mamoe.mirai.console.internal.data

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.internal.plugin.updateWhen
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.currentTimeMillis
import kotlin.reflect.KClass

/**
 * 链接自动保存的 [PluginData].
 * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
 *
 * 若 [AutoSavePluginDataHolder.coroutineContext] 含有 [Job], 则 [AutoSavePluginData] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
 */
internal open class AutoSavePluginData(
    private val owner: AutoSavePluginDataHolder,
    internal val originPluginDataClass: KClass<out PluginData>
) : AbstractPluginData(), PluginConfig {

    private lateinit var storage: PluginDataStorage

    override fun setStorage(storage: PluginDataStorage) {
        check(!this::storage.isInitialized) { "storage is already initialized" }
        this.storage = storage

        if (shouldPerformAutoSaveWheneverChanged()) {
            owner.launch {
                while (isActive) {
                    delay(owner.autoSaveIntervalMillis.last) // 定时自动保存一次, 用于 kts 序列化的对象
                    doSave()
                }
            }
        }
    }

    @JvmField
    @Volatile
    internal var lastAutoSaveJob: Job? = null

    @JvmField
    internal val currentFirstStartTime = atomic(0L)

    protected open fun shouldPerformAutoSaveWheneverChanged(): Boolean {
        return true
    }

    init {
        owner.coroutineContext[Job]?.invokeOnCompletion { doSave() }
    }

    private val updaterBlock: suspend CoroutineScope.() -> Unit = {
        if (::storage.isInitialized) {
            currentFirstStartTime.updateWhen({ it == 0L }, { currentTimeMillis })

            delay(owner.autoSaveIntervalMillis.first.coerceAtLeast(1000)) // for safety

            if (lastAutoSaveJob == this.coroutineContext[Job]) {
                doSave()
            } else {
                if (currentFirstStartTime.updateWhen(
                        { currentTimeMillis - it >= owner.autoSaveIntervalMillis.last },
                        { 0 })
                ) doSave()
            }
        }
    }

    @Suppress("RedundantVisibilityModifier")
    @ConsoleInternalAPI
    public final override fun onValueChanged(value: Value<*>) {
        lastAutoSaveJob = owner.launch(block = updaterBlock)
    }

    private fun doSave() = storage.store(owner, this)
}