/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 可以持有相关 [AutoSavePluginData] 的对象.
 *
 * ### 实现 [AutoSavePluginDataHolder]
 * [CoroutineScope.coroutineContext] 中应用 [CoroutineExceptionHandler]
 *
 * @see net.mamoe.mirai.console.plugin.jvm.JvmPlugin
 */
@ConsoleExperimentalApi
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
    @ConsoleExperimentalApi
    public val autoSaveIntervalMillis: LongRange
}