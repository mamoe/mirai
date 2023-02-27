/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai

import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.BotConfiguration
import kotlin.jvm.JvmSynthetic

/**
 * 构造 [Bot] 的工厂. 这是 [Bot] 唯一的构造方式.
 *
 * @see IMirai.BotFactory
 */
public interface BotFactory {
    /**
     * 相当于 Kotlin lambda `BotConfiguration.() -> Unit` 和 Java `Consumer<BotConfiguration>`
     *
     * @see newBot
     */
    public fun interface BotConfigurationLambda {
        public operator fun BotConfiguration.invoke()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Plain Password
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     *
     * Kotlin:
     * ```
     * newBot(123, "") {
     *     // this: BotConfiguration
     *     fileBasedDeviceInfo()
     * }
     * ```
     *
     * Java:
     * ```java
     * newBot(123, "", configuration -> {
     *     configuration.fileBasedDeviceInfo()
     * })
     * ```
     */
    public fun newBot(
        qq: Long,
        password: String,
        configuration: BotConfigurationLambda /* = BotConfiguration.() -> Unit */
    ): Bot = newBot(qq, password, configuration.run { BotConfiguration().apply { invoke() } })

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String): Bot = newBot(qq, password, BotConfiguration.Default)

    ///////////////////////////////////////////////////////////////////////////
    // MD5 Password
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     *
     * @param passwordMd5 16 bytes
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     *
     * Kotlin:
     * ```
     * newBot(123, password) {
     *     // this: BotConfiguration
     *     fileBasedDeviceInfo()
     * }
     * ```
     *
     * Java:
     * ```java
     * newBot(123, password, configuration -> {
     *     configuration.fileBasedDeviceInfo()
     * })
     * ```
     *
     * @param passwordMd5 16 bytes
     */
    public fun newBot(
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfigurationLambda /* = BotConfiguration.() -> Unit */
    ): Bot = newBot(qq, passwordMd5, configuration.run { BotConfiguration().apply { invoke() } })

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     *
     * @param passwordMd5 16 bytes
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray): Bot = newBot(qq, passwordMd5, BotConfiguration.Default)

    ///////////////////////////////////////////////////////////////////////////
    // BotAuthorization
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     *
     * @since 2.15
     */
    public fun newBot(qq: Long, authorization: BotAuthorization): Bot =
        newBot(qq, authorization, BotConfiguration.Default)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     *
     * @since 2.15
     */
    public fun newBot(qq: Long, authorization: BotAuthorization, configuration: BotConfiguration): Bot


    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     *
     * Kotlin:
     * ```
     * newBot(123, password) {
     *     // this: BotConfiguration
     *     fileBasedDeviceInfo()
     * }
     * ```
     *
     * Java:
     * ```java
     * newBot(123, password, configuration -> {
     *     configuration.fileBasedDeviceInfo()
     * })
     * ```
     *
     * @since 2.15
     */
    public fun newBot(
        qq: Long,
        authorization: BotAuthorization,
        configuration: BotConfigurationLambda /* = BotConfiguration.() -> Unit */
    ): Bot = newBot(qq, authorization, configuration.run { BotConfiguration().apply { invoke() } })


    public companion object INSTANCE : BotFactory {
        override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, password, configuration)
        }

        override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, passwordMd5, configuration)
        }

        /**
         * 使用指定的 [配置][configuration] 构造 [Bot] 实例
         *
         * ```
         * newBot(123, "") {
         *     // this: BotConfiguration
         *     fileBasedDeviceInfo()
         * }
         * ```
         *
         * @since 2.7
         */
        @JvmSynthetic
        public inline fun newBot(
            qq: Long,
            password: String,
            configuration: BotConfiguration.() -> Unit /* = BotConfiguration.() -> Unit */
        ): Bot = newBot(qq, password, BotConfiguration().apply(configuration))

        // implementation notes: this is inline for `inheritCoroutineContext()`
        // see https://github.com/mamoe/mirai/commit/0dbb448cad1ed4773d48ccb8c0b497841bc9fa4c#r50249446

        /**
         * 使用指定的 [配置][configuration] 构造 [Bot] 实例
         *
         * ```
         * newBot(123, password) {
         *     // this: BotConfiguration
         *     fileBasedDeviceInfo()
         * }
         * ```
         *
         * @since 2.7
         */
        @JvmSynthetic
        public inline fun newBot(
            qq: Long,
            passwordMd5: ByteArray,
            configuration: BotConfiguration.() -> Unit /* = BotConfiguration.() -> Unit */
        ): Bot = newBot(qq, passwordMd5, BotConfiguration().apply(configuration))

        override fun newBot(qq: Long, authorization: BotAuthorization, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, authorization, configuration)
        }
    }
}