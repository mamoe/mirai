/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.internal.data.MemoryPluginDataStorageImpl
import net.mamoe.mirai.console.internal.data.MultiFilePluginDataStorageImpl
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * [数据对象][PluginData] 存储仓库.
 *
 * ## 职责
 * [PluginDataStorage] 类似于一个数据库, 它只承担将序列化之后的数据保存到数据库中, 和从数据库取出这个对象的任务.
 * [PluginDataStorage] 不考虑一个 []
 *
 *
 * 此为较低层的 API, 一般插件开发者不会接触.
 *
 * [JarPluginLoader] 实现一个 [PluginDataStorage], 用于管理所有 [JvmPlugin] 的 [PluginData] 实例.
 *
 * @see PluginDataHolder
 * @see JarPluginLoader.dataStorage
 */
public interface PluginDataStorage {
    /**
     * 读取一个实例. 在 [T] 实例创建后 [设置 [PluginDataStorage]][PluginData.setStorage]
     */
    public fun <T : PluginData> load(holder: PluginDataHolder, dataClass: Class<T>): T

    /**
     * 保存一个实例.
     *
     * **实现细节**: 调用 [PluginData.updaterSerializer], 将
     */
    public fun store(holder: PluginDataHolder, pluginData: PluginData)

    public companion object {
        /**
         * 读取一个实例. 在 [T] 实例创建后 [设置 [PluginDataStorage]][PluginData.setStorage]
         */
        @JvmStatic
        public fun <T : PluginData> PluginDataStorage.load(holder: PluginDataHolder, dataClass: KClass<T>): T =
            this.load(holder, dataClass.java)

        /**
         * 读取一个实例. 在 [T] 实例创建后 [设置 [PluginDataStorage]][PluginData.setStorage]
         */
        @JvmSynthetic
        public inline fun <reified T : PluginData> PluginDataStorage.load(holder: PluginDataHolder): T =
            this.load(holder, T::class)
    }
}

/**
 * 在内存存储所有 [PluginData] 实例的 [PluginDataStorage]. 在内存数据丢失后相关 [PluginData] 实例也会丢失.
 * @see PluginDataStorage
 */
public interface MemoryPluginDataStorage : PluginDataStorage, Map<Class<out PluginData>, PluginData> {
    /**
     * 当任一 [PluginData] 实例拥有的 [Value] 的值被改变后调用的回调函数.
     */
    public fun interface OnChangedCallback {
        public fun onChanged(storage: MemoryPluginDataStorage, value: Value<*>)

        /**
         * 无任何操作的 [OnChangedCallback]
         * @see OnChangedCallback
         */
        public object NoOp : OnChangedCallback {
            public override fun onChanged(storage: MemoryPluginDataStorage, value: Value<*>) {
                // no-op
            }
        }
    }

    public companion object {
        /**
         * 创建一个 [MemoryPluginDataStorage] 实例.
         *
         * @param onChanged 当任一 [PluginData] 实例拥有的 [Value] 的值被改变后调用的回调函数.
         */
        @JvmStatic
        @JvmName("create")
        // @JvmOverloads
        public operator fun invoke(onChanged: OnChangedCallback = OnChangedCallback.NoOp): MemoryPluginDataStorage =
            MemoryPluginDataStorageImpl(onChanged)
    }
}

/**
 * 用多个文件存储 [PluginData] 实例的 [PluginDataStorage].
 */
public interface MultiFilePluginDataStorage : PluginDataStorage {
    /**
     * 存放 [PluginData] 的目录.
     */
    public val directoryPath: Path

    public companion object {
        /**
         * 创建一个 [MultiFilePluginDataStorage] 实例.
         *
         * @see directory 存放 [PluginData] 的目录.
         */
        @JvmStatic
        @JvmName("create")
        public operator fun invoke(directory: Path): MultiFilePluginDataStorage =
            MultiFilePluginDataStorageImpl(directory)
    }
}

@get:JvmSynthetic
public inline val MultiFilePluginDataStorage.directory: File
    get() = this.directoryPath.toFile()