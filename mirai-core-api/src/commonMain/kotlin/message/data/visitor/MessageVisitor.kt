/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data.visitor

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 消息的访问器.
 * 优先考虑使用此 visitor API 而不是 [MessageChain] 的 [List] API, 例如 [MessageChain.iterator]. 使用 [MessageVisitor] 将会提升性能, 对于巨大的通过 `plus` 方式连接的消息链有重大区别.
 *
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@OptIn(MiraiExperimentalApi::class)
@MiraiInternalApi
@NotStableForInheritance
public interface MessageVisitor<in D, out R> {
    public fun visitMessage(message: Message, data: D): R

    // region SingleMessage
    public fun visitSingleMessage(message: SingleMessage, data: D): R

    // region MessageContent
    public fun visitMessageContent(message: MessageContent, data: D): R

    public fun visitPlainText(message: PlainText, data: D): R
    public fun visitAt(message: At, data: D): R
    public fun visitAtAll(message: AtAll, data: D): R

    @Suppress("DEPRECATION_ERROR")
    public fun visitVoice(message: net.mamoe.mirai.message.data.Voice, data: D): R
    public fun visitAudio(message: Audio, data: D): R

    public fun visitShortVideo(message: ShortVideo, data: D): R

    // region HummerMessage
    public fun visitHummerMessage(message: HummerMessage, data: D): R
    public fun visitFlashImage(message: FlashImage, data: D): R
    public fun visitPokeMessage(message: PokeMessage, data: D): R
    public fun visitVipFace(message: VipFace, data: D): R
    public fun visitSuperFace(message: SuperFace, data: D): R

    // region MarketFace
    public fun visitMarketFace(message: MarketFace, data: D): R
    public fun visitDice(message: Dice, data: D): R
    public fun visitRockPaperScissors(message: RockPaperScissors, data: D): R

    // endregion
    // endregion

    public fun visitFace(message: Face, data: D): R
    public fun visitFileMessage(message: FileMessage, data: D): R
    public fun visitImage(message: Image, data: D): R
    public fun visitForwardMessage(message: ForwardMessage, data: D): R
    public fun visitMusicShare(message: MusicShare, data: D): R

    // region RichMessage
    public fun visitRichMessage(message: RichMessage, data: D): R
    public fun visitServiceMessage(message: ServiceMessage, data: D): R
    public fun visitSimpleServiceMessage(message: SimpleServiceMessage, data: D): R
    public fun visitLightApp(message: LightApp, data: D): R
    public fun visitAbstractServiceMessage(message: AbstractServiceMessage, data: D): R

    // endregion

    public fun visitUnsupportedMessage(message: UnsupportedMessage, data: D): R
    // endregion

    // region MessageMetadata
    public fun visitMessageMetadata(message: MessageMetadata, data: D): R

    public fun visitMessageOrigin(message: MessageOrigin, data: D): R
    public fun visitMessageSource(message: MessageSource, data: D): R
    public fun visitQuoteReply(message: QuoteReply, data: D): R
    public fun visitCustomMessageMetadata(message: CustomMessageMetadata, data: D): R
    public fun visitShowImageFlag(message: ShowImageFlag, data: D): R

    // endregion

    // endregion

    // region MessageChain
    public fun visitMessageChain(messageChain: MessageChain, data: D): R
    public fun visitCombinedMessage(message: CombinedMessage, data: D): R

    // endregion
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@OptIn(MiraiExperimentalApi::class)
@MiraiInternalApi
public abstract class AbstractMessageVisitor<in D, out R> : MessageVisitor<D, R> {
    public override fun visitSingleMessage(message: SingleMessage, data: D): R {
        return visitMessage(message, data)
    }

    public override fun visitMessageChain(messageChain: MessageChain, data: D): R {
        return visitMessage(messageChain, data)
    }

    public override fun visitCombinedMessage(message: CombinedMessage, data: D): R {
        return visitMessageChain(message, data)
    }


    public override fun visitMessageContent(message: MessageContent, data: D): R {
        return visitSingleMessage(message, data)
    }

    public override fun visitMessageMetadata(message: MessageMetadata, data: D): R {
        return visitSingleMessage(message, data)
    }


    public override fun visitMessageOrigin(message: MessageOrigin, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public override fun visitMessageSource(message: MessageSource, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public override fun visitQuoteReply(message: QuoteReply, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public override fun visitCustomMessageMetadata(message: CustomMessageMetadata, data: D): R {
        return visitMessageMetadata(message, data)
    }

    public override fun visitShowImageFlag(message: ShowImageFlag, data: D): R {
        return visitMessageMetadata(message, data)
    }


    public override fun visitPlainText(message: PlainText, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitAt(message: At, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitAtAll(message: AtAll, data: D): R {
        return visitMessageContent(message, data)
    }

    @Suppress("DEPRECATION_ERROR")
    public override fun visitVoice(message: net.mamoe.mirai.message.data.Voice, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitAudio(message: Audio, data: D): R {
        return visitMessageContent(message, data)
    }

    override fun visitShortVideo(message: ShortVideo, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitHummerMessage(message: HummerMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitFlashImage(message: FlashImage, data: D): R {
        return visitHummerMessage(message, data)
    }

    public override fun visitPokeMessage(message: PokeMessage, data: D): R {
        return visitHummerMessage(message, data)
    }

    public override fun visitVipFace(message: VipFace, data: D): R {
        return visitHummerMessage(message, data)
    }

    override fun visitSuperFace(message: SuperFace, data: D): R {
        return visitHummerMessage(message, data)
    }

    public override fun visitMarketFace(message: MarketFace, data: D): R {
        return visitHummerMessage(message, data)
    }

    public override fun visitDice(message: Dice, data: D): R {
        return visitMarketFace(message, data)
    }

    public override fun visitRockPaperScissors(message: RockPaperScissors, data: D): R {
        return visitMarketFace(message, data)
    }

    public override fun visitFace(message: Face, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitFileMessage(message: FileMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitImage(message: Image, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitForwardMessage(message: ForwardMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitMusicShare(message: MusicShare, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitUnsupportedMessage(message: UnsupportedMessage, data: D): R {
        return visitMessageContent(message, data)
    }


    public override fun visitRichMessage(message: RichMessage, data: D): R {
        return visitMessageContent(message, data)
    }

    public override fun visitServiceMessage(message: ServiceMessage, data: D): R {
        return visitRichMessage(message, data)
    }

    public override fun visitSimpleServiceMessage(message: SimpleServiceMessage, data: D): R {
        return visitServiceMessage(message, data)
    }

    public override fun visitLightApp(message: LightApp, data: D): R {
        return visitRichMessage(message, data)
    }

    public override fun visitAbstractServiceMessage(message: AbstractServiceMessage, data: D): R {
        return visitServiceMessage(message, data)
    }
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@MiraiInternalApi
public abstract class RecursiveMessageVisitor<D> : MessageVisitorUnit() {
    protected open fun isFinished(): Boolean = false

    override fun visitMessage(message: Message, data: Unit) {
        if (isFinished()) return
        message.acceptChildren(this, data)
    }
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@MiraiInternalApi
public abstract class MessageVisitorUnit : AbstractMessageVisitor<Unit, Unit>() {
    override fun visitMessage(message: Message, data: Unit): Unit = Unit
}

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@MiraiInternalApi
public fun <R> Message.accept(visitor: MessageVisitor<Unit, R>): R = this.accept(visitor, Unit)

/**
 * @suppress 这是内部 API, 请不要调用
 * @since 2.12
 */
@MiraiInternalApi
public fun Message.acceptChildren(visitor: MessageVisitor<Unit, *>): Unit = this.acceptChildren(visitor, Unit)