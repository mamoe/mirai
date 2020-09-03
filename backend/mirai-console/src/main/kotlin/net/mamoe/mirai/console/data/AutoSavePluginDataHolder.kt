package net.mamoe.mirai.console.data

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

/**
 * 可以持有相关 [AutoSavePluginData] 的对象.
 *
 * ### 实现 [AutoSavePluginDataHolder]
 * [CoroutineScope.coroutineContext] 中应用 [CoroutineExceptionHandler]
 *
 * @see net.mamoe.mirai.console.plugin.jvm.JvmPlugin
 */
@ConsoleExperimentalAPI
public interface AutoSavePluginDataHolder : PluginDataHolder, CoroutineScope {
    /**
     * [AutoSavePluginData] 每次自动保存时间间隔
     *
     * - 区间的左端点为最小间隔, 一个 [Value] 被修改后, 若此时间段后无其他修改, 将触发自动保存; 若有, 将重新开始计时.
     * - 区间的右端点为最大间隔, 一个 [Value] 被修改后, 最多不超过这个时间段后就会被保存.
     *
     * 若 [AutoSavePluginDataHolder.coroutineContext] 含有 [Job],
     * 则 [AutoSavePluginData] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
     *
     * @see LongRange Java 用户使用 [LongRange] 的构造器创建
     * @see Long.rangeTo Kotlin 用户使用 [Long.rangeTo] 创建, 如 `3000..50000`
     */
    @ConsoleExperimentalAPI
    public val autoSaveIntervalMillis: LongRange
}