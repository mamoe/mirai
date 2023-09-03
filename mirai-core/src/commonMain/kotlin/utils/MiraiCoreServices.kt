/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.utils.Services

internal object MiraiCoreServices {

    @Suppress("RemoveRedundantQualifierName")
    @OptIn(InternalEventMechanism::class)
    fun registerAll() {
        Services.register(
            "net.mamoe.mirai.event.InternalGlobalEventChannelProvider",
            "net.mamoe.mirai.internal.event.GlobalEventChannelProviderImpl"
        ) { net.mamoe.mirai.internal.event.GlobalEventChannelProviderImpl() }

        Services.register(
            "net.mamoe.mirai.IMirai",
            "net.mamoe.mirai.IMirai"
        ) { net.mamoe.mirai.internal.MiraiImpl() }

        val msgProtocol = "net.mamoe.mirai.internal.message.protocol.MessageProtocol"

        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.AudioProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.AudioProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.CustomMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.CustomMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.FaceProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.FaceProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.FileMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.FileMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.FlashImageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.FlashImageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.IgnoredMessagesProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.IgnoredMessagesProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.ImageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.ImageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.MarketFaceProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.MarketFaceProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.SuperFaceProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.SuperFaceProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.MusicShareProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.MusicShareProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.PokeMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.PokeMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.PttMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.PttMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.QuoteReplyProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.QuoteReplyProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.RichMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.RichMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.ShortVideoProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.ShortVideoProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.TextProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.TextProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.VipFaceProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.VipFaceProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.ForwardMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.ForwardMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.LongMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.LongMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.UnsupportedMessageProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.UnsupportedMessageProtocol() }
        Services.register(
            msgProtocol,
            "net.mamoe.mirai.internal.message.protocol.impl.GeneralMessageSenderProtocol"
        ) { net.mamoe.mirai.internal.message.protocol.impl.GeneralMessageSenderProtocol() }


        Services.register(
            "net.mamoe.mirai.message.data.InternalImageProtocol",
            "net.mamoe.mirai.internal.message.image.InternalImageProtocolImpl"
        ) { net.mamoe.mirai.internal.message.image.InternalImageProtocolImpl() }

        Services.register(
            "net.mamoe.mirai.message.data.OfflineAudio.Factory",
            "net.mamoe.mirai.internal.message.data.OfflineAudioFactoryImpl"
        ) { net.mamoe.mirai.internal.message.data.OfflineAudioFactoryImpl() }

        Services.register(
            "net.mamoe.mirai.auth.DefaultBotAuthorizationFactory",
            "net.mamoe.mirai.internal.network.auth.DefaultBotAuthorizationFactoryImpl"
        ) { net.mamoe.mirai.internal.network.auth.DefaultBotAuthorizationFactoryImpl() }

        Services.register(
            "net.mamoe.mirai.utils.InternalProtocolDataExchange",
            "net.mamoe.mirai.internal.utils.MiraiProtocolInternal\$Exchange"
        ) { net.mamoe.mirai.internal.utils.MiraiProtocolInternal.Exchange() }
    }
}