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

import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 可以持有相关 [PluginData] 实例的对象, 作为 [PluginData] 实例的拥有者.
 *
 * @see PluginDataStorage.load
 * @see PluginDataStorage.store
 *
 * @see AutoSavePluginDataHolder 支持自动保存
 */
@ConsoleExperimentalApi
public interface PluginDataHolder {
    /**
     * 保存时使用的分类名
     */
    @ConsoleExperimentalApi
    public val dataHolderName: String
}

/*
public interface PluginDataHolder {

    /**
     * 创建一个 [PluginData] 实例.
     *
     * 注意, 此时的 [PluginData] 并没有绑定 [PluginDataStorage], 因此无法进行保存等操作.
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
 */

/*
public interface AutoSavePluginDataHolder : PluginDataHolder, CoroutineScope {

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
 */