/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data.visitor

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * @suppress 这是内部 API, 请不要调用
 * @since MESSAGE_VISITOR
 */
@MiraiInternalApi
public interface MessageVisitor<in D, out R> {
    public fun visitMessage(message: Message, data: D): R


    public fun visitSingleMessage(message: SingleMessage, data: D): R {
        return visitMessage(message, data)
    }

    public fun visitMessageChain(messageChain: MessageChain, data: D): R {
        return visitMessage(messageChain, data)
    }


    public fun visitMessageContent(message: MessageContent, data: D): R {
        return visitSingleMessage(message, data)
    }

    public fun visitMessageMetadata(message: MessageMetadata, data: D): R {
        return visitSingleMessage(message, data)
    }


    public fun visitMessageOrigin(message: MessageOrigin, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public fun visitMessageSource(message: MessageSource, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public fun visitQuoteReply(message: QuoteReply, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public fun visitCustomMessageMetadata(message: CustomMessageMetadata, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public fun visitShowImageFlag(message: ShowImageFlag, data: D): R {
        return visitMessageMetadata(message, data)
    }


    public fun visitPlainText(message: PlainText, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitAt(message: At, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitAtAll(message: AtAll, data: D): R {
        return visitMessageContent(message, data)
    }

    @Suppress("DEPRECATION_ERROR")
    public fun visitVoice(message: Voice, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitAudio(message: Audio, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitHummerMessage(message: HummerMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitFlashImage(message: FlashImage, data: D): R {
        return visitHummerMessage(message, data)
    }

    public fun visitPokeMessage(message: PokeMessage, data: D): R {
        return visitHummerMessage(message, data)
    }

    public fun visitVipFace(message: VipFace, data: D): R {
        return visitHummerMessage(message, data)
    }

    public fun visitMarketFace(message: MarketFace, data: D): R {
        return visitHummerMessage(message, data)
    }

    public fun visitDice(message: Dice, data: D): R {
        return visitMarketFace(message, data)
    }

    public fun visitFace(message: Face, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitFileMessage(message: FileMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitImage(message: Image, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitForwardMessage(message: ForwardMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitMusicShare(message: MusicShare, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitUnsupportedMessage(message: UnsupportedMessage, data: D): R {
        return visitMessageContent(message, data)
    }


    public fun visitRichMessage(message: RichMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public fun visitServiceMessage(message: ServiceMessage, data: D): R {
        return visitRichMessage(message, data)
    }

    public fun visitSimpleServiceMessage(message: SimpleServiceMessage, data: D): R {
        return visitServiceMessage(message, data)
    }

    public fun visitLightApp(message: LightApp, data: D): R {
        return visitRichMessage(message, data)
    }

    public fun visitAbstractServiceMessage(message: AbstractServiceMessage, data: D): R {
        return visitServiceMessage(message, data)
    }
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.11
 */
@MiraiInternalApi
public interface MessageVisitorUnit<in D> : MessageVisitor<D, Unit> {
    override fun visitMessage(message: Message, data: D): Unit = Unit
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.11
 */
@MiraiInternalApi
public fun <R> Message.accept(visitor: MessageVisitor<Unit, R>): R = this.accept(visitor, Unit)