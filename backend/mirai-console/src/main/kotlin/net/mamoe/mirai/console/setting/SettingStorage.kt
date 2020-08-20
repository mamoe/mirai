/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package net.mamoe.mirai.console.setting

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.internal.setting.*
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.setting.SettingStorage.Companion.load
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * [Setting] 存储容器.
 *
 * 此为较低层的 API, 一般插件开发者不会接触.
 *
 * [JarPluginLoader] 实现一个 [SettingStorage], 用于管理所有 [JvmPlugin] 的 [Setting] 实例.
 *
 * @see SettingHolder
 * @see JarPluginLoader.settingStorage
 */
public interface SettingStorage {
    /**
     * 读取一个实例. 在 [T] 实例创建后 [设置 [SettingStorage]][Setting.setStorage]
     */
    public fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T

    /**
     * 保存一个实例
     */
    public fun store(holder: SettingHolder, setting: Setting)

    public companion object {
        /**
         * 读取一个实例. 在 [T] 实例创建后 [设置 [SettingStorage]][Setting.setStorage]
         */
        @JvmStatic
        public fun <T : Setting> SettingStorage.load(holder: SettingHolder, settingClass: KClass<T>): T =
            this.load(holder, settingClass.java)

        /**
         * 读取一个实例. 在 [T] 实例创建后 [设置 [SettingStorage]][Setting.setStorage]
         */
        @JvmSynthetic
        public inline fun <reified T : Setting> SettingStorage.load(holder: SettingHolder): T =
            this.load(holder, T::class)
    }
}

/**
 * 在内存存储所有 [Setting] 实例的 [SettingStorage]. 在内存数据丢失后相关 [Setting] 实例也会丢失.
 */
public interface MemorySettingStorage : SettingStorage, Map<Class<out Setting>, Setting> {
    /**
     * 当任一 [Setting] 实例拥有的 [Value] 的值被改变后调用的回调函数.
     */
    public fun interface OnChangedCallback {
        public fun onChanged(storage: MemorySettingStorage, value: Value<*>)

        /**
         * 无任何操作的 [OnChangedCallback]
         * @see OnChangedCallback
         */
        public object NoOp : OnChangedCallback {
            public override fun onChanged(storage: MemorySettingStorage, value: Value<*>) {
                // no-op
            }
        }
    }

    public companion object {
        /**
         * 创建一个 [MemorySettingStorage] 实例.
         *
         * @param onChanged 当任一 [Setting] 实例拥有的 [Value] 的值被改变后调用的回调函数.
         */
        @JvmStatic
        @JvmName("create")
        // @JvmOverloads
        public operator fun invoke(onChanged: OnChangedCallback = OnChangedCallback.NoOp): MemorySettingStorage =
            MemorySettingStorageImpl(onChanged)
    }
}

/**
 * 用多个文件存储 [Setting] 实例的 [SettingStorage].
 */
public interface MultiFileSettingStorage : SettingStorage {
    /**
     * 存放 [Setting] 的目录.
     */
    public val directory: File

    public companion object {
        /**
         * 创建一个 [MultiFileSettingStorage] 实例.
         *
         * @see directory 存放 [Setting] 的目录.
         */
        @JvmStatic
        @JvmName("create")
        public operator fun invoke(directory: File): MultiFileSettingStorage = MultiFileSettingStorageImpl(directory)
    }
}

/**
 * 可以持有相关 [Setting] 实例的对象, 作为 [Setting] 实例的拥有者.
 *
 * @see SettingStorage.load
 * @see SettingStorage.store
 *
 * @see AutoSaveSettingHolder 自动保存
 */
public interface SettingHolder {
    /**
     * 保存时使用的分类名
     */
    public val name: String

    /**
     * 创建一个 [Setting] 实例.
     *
     * @see Companion.newSettingInstance
     * @see KClass.createType
     */
    @JvmDefault
    public fun <T : Setting> newSettingInstance(type: KType): T =
        newSettingInstanceUsingReflection<Setting>(type) as T

    public companion object {
        /**
         * 创建一个 [Setting] 实例.
         *
         * @see SettingHolder.newSettingInstance
         */
        @JvmSynthetic
        public inline fun <reified T : Setting> SettingHolder.newSettingInstance(): T {
            return this.newSettingInstance(typeOf0<T>())
        }
    }
}

/**
 * 可以持有相关 [AutoSaveSetting] 的对象.
 *
 * @see net.mamoe.mirai.console.plugin.jvm.JvmPlugin
 */
public interface AutoSaveSettingHolder : SettingHolder, CoroutineScope {
    /**
     * [AutoSaveSetting] 每次自动保存时间间隔
     *
     * - 区间的左端点为最小间隔, 一个 [Value] 被修改后, 若此时间段后无其他修改, 将触发自动保存; 若有, 将重新开始计时.
     * - 区间的右端点为最大间隔, 一个 [Value] 被修改后, 最多不超过这个时间段后就会被保存.
     *
     * 若 [AutoSaveSettingHolder.coroutineContext] 含有 [Job],
     * 则 [AutoSaveSetting] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
     *
     * @see LongRange Java 用户使用 [LongRange] 的构造器创建
     * @see Long.rangeTo Kotlin 用户使用 [Long.rangeTo] 创建, 如 `3000..50000`
     */
    public val autoSaveIntervalMillis: LongRange

    /**
     * 仅支持确切的 [Setting] 类型
     */
    @JvmDefault
    public override fun <T : Setting> newSettingInstance(type: KType): T {
        val classifier = type.classifier?.cast<KClass<*>>()?.java
        require(classifier == Setting::class.java) {
            "Cannot create Setting instance. AutoSaveSettingHolder supports only Setting type."
        }
        return AutoSaveSetting(this) as T // T is always Setting
    }
}