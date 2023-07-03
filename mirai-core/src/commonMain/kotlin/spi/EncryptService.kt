/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.spi

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SpiServiceLoader
import net.mamoe.mirai.utils.*


/**
 * @since 2.15.0
 */
public class EncryptServiceContext @MiraiInternalApi constructor(
    /**
     * [Bot.id]
     */
    public val id: Long,
    public val extraArgs: TypeSafeMap = TypeSafeMap.EMPTY
) {
    public companion object {
        public val KEY_COMMAND_STR: TypeKey<String> = TypeKey("KEY_COMMAND_STR")
        public val KEY_BOT_PROTOCOL: TypeKey<BotConfiguration.MiraiProtocol> = TypeKey("BOT_PROTOCOL")
        public val KEY_CHANNEL_PROXY: TypeKey<EncryptService.ChannelProxy> = TypeKey("KEY_CHANNEL_PROXY")
        public val KEY_DEVICE_INFO: TypeKey<DeviceInfo> = TypeKey("KEY_DEVICE_INFO")
        public val KEY_QIMEI36: TypeKey<String> = TypeKey("KEY_QIMEI36")
        public val KEY_BOT_WORKING_DIR: TypeKey<String> = TypeKey("KEY_BOT_WORKING_DIR")
        public val KEY_BOT_CACHING_DIR: TypeKey<String> = TypeKey("KEY_BOT_CACHING_DIR")
    }
}

/**
 * @since 2.15.0
 */
public interface EncryptService {
    public fun initialize(context: EncryptServiceContext)

    /**
     * Returns `false` if not supported.
     */
    public fun supports(protocol: BotConfiguration.MiraiProtocol): Boolean {
        return protocol != BotConfiguration.MiraiProtocol.ANDROID_WATCH
    }

    /**
     * Returns `null` if encrypt fail.
     */
    public fun encryptTlv(
        context: EncryptServiceContext,
        tlvType: Int,
        payload: ByteArray, // Do not write to payload
    ): ByteArray?

    public fun qSecurityGetSign(
        context: EncryptServiceContext,
        sequenceId: Int,
        commandName: String,
        payload: ByteArray
    ): SignResult?

    public class SignResult(
        public val sign: ByteArray = EMPTY_BYTE_ARRAY,
        public val token: ByteArray = EMPTY_BYTE_ARRAY,
        public val extra: ByteArray = EMPTY_BYTE_ARRAY,
    )

    public class ChannelResult(
        public val cmd: String,
        public val data: ByteArray,
    )

    public interface ChannelProxy {
        public suspend fun sendMessage(remark: String, commandName: String, uin: Long, data: ByteArray): ChannelResult?
    }

    // net.mamoe.mirai.internal.spi.EncryptService$Factory
    public interface Factory : BaseService {

        /*
         * cleanup:
         *         serviceSubScope.coroutineContext.job.invokeOnCompletion { }
         */
        public fun createForBot(context: EncryptServiceContext, serviceSubScope: CoroutineScope): EncryptService
    }


    public companion object {

        private val loader = SpiServiceLoader(Factory::class)

        private val warningAlert: Unit by lazy {
            val log = MiraiLogger.Factory.create(EncryptService::class, "EncryptService.alert")

            val serviceUsed = loader.service

            if (serviceUsed != null) {
                val serviceClass = serviceUsed.javaClass
                log.warning { "Encrypt service was loaded: $serviceUsed" }
                log.warning { "All outgoing message may be leaked by this service." }
                log.warning { "Use this service if and only if you trusted this service and the service provider." }
                log.warning { "Service details:" }
                log.warning { "  `- Jvm Class: $serviceClass" }
                log.warning { "  `- ClassLoader: " + serviceClass.classLoader }
                log.warning { "  `- Source: " + serviceClass.protectionDomain?.codeSource?.location }
                log.warning { "  `- Protected Domain: " + serviceClass.protectionDomain }
            }

        }

        internal val factory: Factory?
            get() {
                warningAlert
                return loader.service
            }
    }

    // special error: no service used
    public object SignalServiceNotAvailable : RuntimeException()
}
