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
        public val KEY_APP_QUA: TypeKey<String> = TypeKey("KEY_APP_QUA")
        public val KEY_CHANNEL_PROXY: TypeKey<EncryptService.ChannelProxy> = TypeKey("KEY_CHANNEL_PROXY")
    }
}

/**
 * @since 2.15.0
 */
public interface EncryptService : BaseService {
    public fun initialize(context: EncryptServiceContext)

    /**
     * Returns `null` if not supported.
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

    public companion object {
        private val loader = SpiServiceLoader(EncryptService::class)

        internal val instance: EncryptService? get() = loader.service
    }

}
