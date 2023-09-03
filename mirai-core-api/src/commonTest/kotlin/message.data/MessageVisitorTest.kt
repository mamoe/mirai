/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.message.data.visitor.AbstractMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class MessageVisitorTest {
    object GetCalledMethodNames : AbstractMessageVisitor<Unit, Array<String>>() {
        override fun visitMessage(message: Message, data: Unit): Array<String> {
            return arrayOf("visitMessage")
        }

        override fun visitSingleMessage(message: SingleMessage, data: Unit): Array<String> {
            return arrayOf("visitSingleMessage") + super.visitSingleMessage(message, data)
        }

        override fun visitMessageChain(messageChain: MessageChain, data: Unit): Array<String> {
            return arrayOf("visitMessageChain") + super.visitMessageChain(messageChain, data)
        }

        override fun visitCombinedMessage(message: CombinedMessage, data: Unit): Array<String> {
            return arrayOf("visitCombinedMessage") + super.visitCombinedMessage(message, data)
        }

        override fun visitMessageContent(message: MessageContent, data: Unit): Array<String> {
            return arrayOf("visitMessageContent") + super.visitMessageContent(message, data)
        }

        override fun visitMessageMetadata(message: MessageMetadata, data: Unit): Array<String> {
            return arrayOf("visitMessageMetadata") + super.visitMessageMetadata(message, data)
        }

        override fun visitMessageOrigin(message: MessageOrigin, data: Unit): Array<String> {
            return arrayOf("visitMessageOrigin") + super.visitMessageOrigin(message, data)
        }

        override fun visitMessageSource(message: MessageSource, data: Unit): Array<String> {
            return arrayOf("visitMessageSource") + super.visitMessageSource(message, data)
        }

        override fun visitQuoteReply(message: QuoteReply, data: Unit): Array<String> {
            return arrayOf("visitQuoteReply") + super.visitQuoteReply(message, data)
        }

        override fun visitCustomMessageMetadata(message: CustomMessageMetadata, data: Unit): Array<String> {
            return arrayOf("visitCustomMessageMetadata") + super.visitCustomMessageMetadata(message, data)
        }

        override fun visitShowImageFlag(message: ShowImageFlag, data: Unit): Array<String> {
            return arrayOf("visitShowImageFlag") + super.visitShowImageFlag(message, data)
        }

        override fun visitPlainText(message: PlainText, data: Unit): Array<String> {
            return arrayOf("visitPlainText") + super.visitPlainText(message, data)
        }

        override fun visitAt(message: At, data: Unit): Array<String> {
            return arrayOf("visitAt") + super.visitAt(message, data)
        }

        override fun visitAtAll(message: AtAll, data: Unit): Array<String> {
            return arrayOf("visitAtAll") + super.visitAtAll(message, data)
        }

        @Suppress("DEPRECATION_ERROR")
        override fun visitVoice(message: Voice, data: Unit): Array<String> {
            return arrayOf("visitVoice") + super.visitVoice(message, data)
        }

        override fun visitAudio(message: Audio, data: Unit): Array<String> {
            return arrayOf("visitAudio") + super.visitAudio(message, data)
        }

        override fun visitHummerMessage(message: HummerMessage, data: Unit): Array<String> {
            return arrayOf("visitHummerMessage") + super.visitHummerMessage(message, data)
        }

        override fun visitFlashImage(message: FlashImage, data: Unit): Array<String> {
            return arrayOf("visitFlashImage") + super.visitFlashImage(message, data)
        }

        override fun visitPokeMessage(message: PokeMessage, data: Unit): Array<String> {
            return arrayOf("visitPokeMessage") + super.visitPokeMessage(message, data)
        }

        override fun visitVipFace(message: VipFace, data: Unit): Array<String> {
            return arrayOf("visitVipFace") + super.visitVipFace(message, data)
        }

        override fun visitMarketFace(message: MarketFace, data: Unit): Array<String> {
            return arrayOf("visitMarketFace") + super.visitMarketFace(message, data)
        }

        override fun visitDice(message: Dice, data: Unit): Array<String> {
            return arrayOf("visitDice") + super.visitDice(message, data)
        }

        override fun visitRockPaperScissors(message: RockPaperScissors, data: Unit): Array<String> {
            return arrayOf("visitRockPaperScissors") + super.visitRockPaperScissors(message, data)
        }

        override fun visitFace(message: Face, data: Unit): Array<String> {
            return arrayOf("visitFace") + super.visitFace(message, data)
        }

        override fun visitSuperFace(message: SuperFace, data: Unit): Array<String> {
            return arrayOf("visitSuperFace") + super.visitSuperFace(message, data)
        }

        override fun visitFileMessage(message: FileMessage, data: Unit): Array<String> {
            return arrayOf("visitFileMessage") + super.visitFileMessage(message, data)
        }

        override fun visitImage(message: Image, data: Unit): Array<String> {
            return arrayOf("visitImage") + super.visitImage(message, data)
        }

        override fun visitForwardMessage(message: ForwardMessage, data: Unit): Array<String> {
            return arrayOf("visitForwardMessage") + super.visitForwardMessage(message, data)
        }

        override fun visitMusicShare(message: MusicShare, data: Unit): Array<String> {
            return arrayOf("visitMusicShare") + super.visitMusicShare(message, data)
        }

        override fun visitUnsupportedMessage(message: UnsupportedMessage, data: Unit): Array<String> {
            return arrayOf("visitUnsupportedMessage") + super.visitUnsupportedMessage(message, data)
        }

        override fun visitRichMessage(message: RichMessage, data: Unit): Array<String> {
            return arrayOf("visitRichMessage") + super.visitRichMessage(message, data)
        }

        override fun visitServiceMessage(message: ServiceMessage, data: Unit): Array<String> {
            return arrayOf("visitServiceMessage") + super.visitServiceMessage(message, data)
        }

        override fun visitSimpleServiceMessage(message: SimpleServiceMessage, data: Unit): Array<String> {
            return arrayOf("visitSimpleServiceMessage") + super.visitSimpleServiceMessage(message, data)
        }

        override fun visitLightApp(message: LightApp, data: Unit): Array<String> {
            return arrayOf("visitLightApp") + super.visitLightApp(message, data)
        }

        override fun visitAbstractServiceMessage(message: AbstractServiceMessage, data: Unit): Array<String> {
            return arrayOf("visitAbstractServiceMessage") + super.visitAbstractServiceMessage(message, data)
        }
    }

    @OptIn(MessageChainConstructor::class)
    @Test
    fun visitMessageChain() {
        assertContentEquals(
            arrayOf(
                "visitMessageChain",
                "visitMessage",
            ),
            messageChainOf(PlainText("1")).accept(GetCalledMethodNames)
        )
        assertContentEquals(
            arrayOf(
                "visitMessageChain",
                "visitMessage",
            ),
            emptyMessageChain().accept(GetCalledMethodNames)
        )
        assertContentEquals(
            arrayOf(
                "visitCombinedMessage",
                "visitMessageChain",
                "visitMessage",
            ),
            CombinedMessage(AtAll, AtAll, false).accept(GetCalledMethodNames)
        )
    }

    @Test
    fun visitSingleMessageContent() {
        assertContentEquals(
            arrayOf(
                "visitMessage",
            ),
            object : Message {
                @Suppress("RedundantOverride") // false positive
                override fun toString(): String = super.toString()
                override fun contentToString(): String = super.toString()
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitSingleMessage",
                "visitMessage",
            ),
            object : SingleMessage {
                @Suppress("RedundantOverride") // false positive
                override fun toString(): String = super.toString()
                override fun contentToString(): String = super.toString()
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            object : MessageContent {
                @Suppress("RedundantOverride") // false positive
                override fun toString(): String = super.toString()
                override fun contentToString(): String = super.toString()
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitPlainText",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            PlainText("1").accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitAt",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            At(1).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitAtAll",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            AtAll.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitVoice",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            @Suppress("DEPRECATION_ERROR")
            Voice("", byteArrayOf(), 1, 1, "").accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitAudio",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            object : OfflineAudio {
                override val filename: String get() = ""
                override val fileMd5: ByteArray get() = byteArrayOf()
                override val fileSize: Long get() = 1
                override val codec: AudioCodec get() = AudioCodec.AMR
                override val extraData: ByteArray? get() = null
                override fun toString(): String = "test"
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitFlashImage",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            FlashImage(createImage()).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitSuperFace",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            SuperFace.from(Face(Face.DA_CALL)).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitPokeMessage",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            PokeMessage.BiXin.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitVipFace",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            VipFace(VipFace.AiXin, 1).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitMarketFace",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            object : MarketFace {
                override val name: String get() = "f"
                override val id: Int get() = 1
                override fun toString(): String = "ok"
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitDice",
                "visitMarketFace",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            Dice(1).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitRockPaperScissors",
                "visitMarketFace",
                "visitHummerMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            RockPaperScissors.PAPER.accept(GetCalledMethodNames)
        )


        assertContentEquals(
            arrayOf(
                "visitFace",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            Face(Face.AI_NI).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitFileMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            object : FileMessage {
                override val id: String get() = ""
                override val internalId: Int get() = 1
                override val name: String get() = ""
                override val size: Long get() = 1

                override suspend fun toAbsoluteFile(contact: FileSupported): Nothing =
                    throw UnsupportedOperationException()

                override fun toString(): String = ""
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitImage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            createImage().accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitForwardMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            ForwardMessage(listOf(), "", "", "", "", listOf()).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitMusicShare",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            MusicShare(
                kind = MusicKind.NeteaseCloudMusic,
                title = "ファッション",
                summary = "rinahamu/Yunomi",
                brief = "",
                jumpUrl = "https://music.163.com/song/1338728297/?userid=324076307",
                pictureUrl = "https://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
                musicUrl = "https://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"
            ).accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitUnsupportedMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            object : UnsupportedMessage {
                override val struct: ByteArray get() = byteArrayOf()
                override fun toString(): String = "test"
            }.accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitSimpleServiceMessage",
                "visitServiceMessage",
                "visitRichMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            SimpleServiceMessage(1, "str").accept(GetCalledMethodNames)
        )

        assertContentEquals(
            arrayOf(
                "visitLightApp",
                "visitRichMessage",
                "visitMessageContent",
                "visitSingleMessage",
                "visitMessage",
            ),
            LightApp("str").accept(GetCalledMethodNames)
        )
    }
}

private fun createImage(): Image {
    return object : Image {
        override val imageId: String get() = "{88914B32-B758-74ED-B00D-CAA6D2A5D7F6}.jpg"
        override val width: Int get() = 1
        override val height: Int get() = 1
        override val size: Long get() = 1
        override val imageType: ImageType get() = ImageType.APNG

        override fun toString(): String = "test"
        override fun contentToString(): String = "test"

        @MiraiExperimentalApi
        override fun appendMiraiCodeTo(builder: StringBuilder) {
        }

    }
}