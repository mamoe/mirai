/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.utils.JavaPluginScheduler
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Java 插件的父类
 */
abstract class JavaPlugin @JvmOverloads constructor(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : JvmPlugin, AbstractJvmPlugin(parentCoroutineContext) {

    /*

    @Volatile
    internal var lastAutoSaveJob: Job? = null

    @Volatile
    internal var currentFirstStartTime = atomic(0L)

    /**
     * [PluginSetting] 每次自动保存时间间隔
     *
     * - 区间的左端点为最小间隔, 一个 [Value] 被修改后, 若此时间段后无其他修改, 将触发自动保存; 若有, 将重新开始计时.
     * - 区间的右端点为最大间隔, 一个 [Value] 被修改后, 最多不超过这个时间段后就会被保存.
     *
     * 备注: 当插件被关闭时, 所有相关 [PluginSetting] 总是会被自动保存.
     */
    open val autoSaveIntervalMillis: LongRange
        get() = 30.secondsToMillis..10.minutesToSeconds

    /**
     * 链接自动保存的 [Setting].
     * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
     */
    abstract inner class PluginSetting : AbstractSetting() {
        init {

            this@AbstractJvmPlugin.job.invokeOnCompletion {
                doSave()
            }
        }

        final override fun onValueChanged(value: Value<*>) {
            lastAutoSaveJob = launch {
                currentFirstStartTime.updateWhen({ it == 0L }, { currentTimeMillis })

                delay(autoSaveIntervalMillis.first.coerceAtLeast(1000)) // for safety

                if (lastAutoSaveJob == job) {
                    doSave()
                } else {
                    if (currentFirstStartTime.updateWhen(
                            { currentTimeMillis - it >= autoSaveIntervalMillis.last },
                            { 0 })
                    ) {
                        doSave()
                    }
                }
            }
        }

        private fun doSave() {
            loader.settingStorage.store(this@AbstractJvmPlugin, this@PluginSetting)
        }
    }
    */

    /**
     * Java API Scheduler
     */
    val scheduler: JavaPluginScheduler =
        JavaPluginScheduler(this.coroutineContext)
}