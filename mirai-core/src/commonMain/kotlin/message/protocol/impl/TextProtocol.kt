/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("TextProtocol_common")

package net.mamoe.mirai.internal.message.protocol.impl

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.isForward
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.originalMessage
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.safeCast
import kotlin.jvm.JvmName

/**
 * For [PlainText] and [At]
 */
internal class TextProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(PlainTextEncoder())
        add(AtEncoder())
        add(AtAllEncoder())

        add(Decoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(PlainText::class, PlainText.serializer(), registerAlsoContextual = true))
            add(MessageSerializer(At::class, At.serializer(), registerAlsoContextual = true))
            add(MessageSerializer(AtAll::class, AtAll.serializer(), registerAlsoContextual = true))
            add(MessageSerializer(Face::class, Face.serializer(), registerAlsoContextual = true))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val text = data.text ?: return
            markAsConsumed()
            if (text.attr6Buf.isEmpty()) {
                collect(PlainText(text.str))
            } else {
                val id = text.attr6Buf.read {
                    discardExact(7)
                    readInt().toUInt().toLong()
                }
                if (id == 0L) {
                    collect(AtAll)
                } else {
                    collect(At(id)) // element.text.str
                }
            }

        }
    }

    private class PlainTextEncoder : MessageEncoder<PlainText> {
        override suspend fun MessageEncoderContext.process(data: PlainText) {
            markAsConsumed()
            collect(ImMsgBody.Elem(text = ImMsgBody.Text(str = data.content)))
        }
    }

    private class AtEncoder : MessageEncoder<At> {
        override suspend fun MessageEncoderContext.process(data: At) {
            markAsConsumed()
            collected += ImMsgBody.Elem(
                text = data.toJceData(
                    attributes[CONTACT].safeCast(),
                    originalMessage.sourceOrNull,
                    isForward,
                )
            )
            // elements.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = " ")))
            // removed by https://github.com/mamoe/mirai/issues/524
            // 发送 QuoteReply 消息时无可避免的产生多余空格 #524
        }

        private fun At.toJceData(
            group: Group?,
            source: MessageSource?,
            isForward: Boolean,
        ): ImMsgBody.Text {
            fun findFromGroup(g: Group?): String? {
                return g?.members?.get(this.target)?.nameCardOrNick
            }

            fun findFromSource(): String? {
                return when (source) {
                    is OnlineMessageSource -> {
                        return findFromGroup(source.target.safeCast())
                    }
                    is OfflineMessageSource -> {
                        if (source.kind == MessageSourceKind.GROUP) {
                            return findFromGroup(group?.bot?.getGroup(source.targetId))
                        } else null
                    }
                    else -> null
                }
            }

            val nick = if (isForward) {
                findFromSource() ?: findFromGroup(group)
            } else {
                findFromGroup(group) ?: findFromSource()
            } ?: target.toString()

            val text = "@$nick".dropEmoji()
            return ImMsgBody.Text(
                str = text,
                attr6Buf = buildPacket {
                    // MessageForText$AtTroopMemberInfo
                    writeShort(1) // const
                    writeShort(0) // startPos
                    writeShort(text.length.toShort()) // textLen
                    writeByte(0) // flag, may=1
                    writeInt(target.toInt()) // uin
                    writeShort(0) // const
                }.readBytes()
            )
        }

        companion object {
            // region Emoji pattern. <Licenced under the MIT LICENSE>
            //
            // https://github.com/mathiasbynens/emoji-test-regex-pattern
            // https://github.com/mathiasbynens/emoji-test-regex-pattern/blob/main/dist/latest/java.txt
            //

            @Suppress("RegExpSingleCharAlternation", "RegExpRedundantEscape")
            private val EMOJI_PATTERN: Regex? = runCatching {
                val resource = getEmojiPatternResourceOrNull() ?: return@runCatching null
                Regex(resource)
            }.getOrNull() // May some java runtime unsupported

            fun String.dropEmoji(): String {
                EMOJI_PATTERN?.let { regex -> return replace(regex, "") }
                return this
            }

            // endregion
        }
    }

    private class AtAllEncoder : MessageEncoder<AtAll> {
        override suspend fun MessageEncoderContext.process(data: AtAll) {
            markAsConsumed()
            collect(jceData)
        }

        companion object {
            private val jceData by lazy {
                ImMsgBody.Elem(
                    text = ImMsgBody.Text(
                        str = AtAll.display,
                        attr6Buf = buildPacket {
                            // MessageForText$AtTroopMemberInfo
                            writeShort(1) // const
                            writeShort(0) // startPos
                            writeShort(AtAll.display.length.toShort()) // textLen
                            writeByte(1) // flag, may=1
                            writeInt(0) // uin
                            writeShort(0) // const
                        }.readBytes()
                    )
                )
            }
        }
    }
}

internal expect fun getEmojiPatternResourceOrNull(): String?