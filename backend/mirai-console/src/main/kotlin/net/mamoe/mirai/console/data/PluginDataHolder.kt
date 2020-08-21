@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package net.mamoe.mirai.console.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.data.PluginDataStorage.Companion.load
import net.mamoe.mirai.console.internal.data.AutoSavePluginData
import net.mamoe.mirai.console.internal.data.cast
import net.mamoe.mirai.console.internal.data.newPluginDataInstanceUsingReflection
import net.mamoe.mirai.console.internal.data.typeOf0
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * 可以持有相关 [PluginData] 实例的对象, 作为 [PluginData] 实例的拥有者.
 *
 * @see PluginDataStorage.load
 * @see PluginDataStorage.store
 *
 * @see AutoSavePluginDataHolder 自动保存
 */
public interface PluginDataHolder {
    /**
     * 保存时使用的分类名
     */
    public val name: String

    /**
     * 创建一个 [PluginData] 实例.
     *
     * @see Companion.newPluginDataInstance
     * @see KClass.createType
     */
    @JvmDefault
    public fun <T : PluginData> newPluginDataInstance(type: KType): T =
        newPluginDataInstanceUsingReflection<PluginData>(type) as T

    public companion object {
        /**
         * 创建一个 [PluginData] 实例.
         *
         * @see PluginDataHolder.newPluginDataInstance
         */
        @JvmSynthetic
        public inline fun <reified T : PluginData> PluginDataHolder.newPluginDataInstance(): T {
            return this.newPluginDataInstance(typeOf0<T>())
        }
    }
}

/**
 * 可以持有相关 [AutoSavePluginData] 的对象.
 *
 * @see net.mamoe.mirai.console.plugin.jvm.JvmPlugin
 */
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
    public val autoSaveIntervalMillis: LongRange

    /**
     * 仅支持确切的 [PluginData] 类型
     */
    @JvmDefault
    public override fun <T : PluginData> newPluginDataInstance(type: KType): T {
        val classifier = type.classifier?.cast<KClass<PluginData>>()
        require(classifier != null && classifier.java == PluginData::class.java) {
            "Cannot create PluginData instance. AutoSavePluginDataHolder supports only PluginData type."
        }
        return AutoSavePluginData(this, classifier) as T // T is always PluginData
    }
}