/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.hexToBytes


internal class MarketFaceProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(DiceEncoder())
        add(MarketFaceImplEncoder())

        add(MarketFaceDecoder())

        MessageSerializer.superclassesScope(MarketFace::class, MessageContent::class, SingleMessage::class) {
            add(
                MessageSerializer(
                    MarketFaceImpl::class,
                    MarketFaceImpl.serializer()
                )
            )
        }

        MessageSerializer.superclassesScope(MarketFace::class, MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(Dice::class, Dice.serializer()))
        }
    }

    private class MarketFaceImplEncoder : MessageEncoder<MarketFaceImpl> {
        override suspend fun MessageEncoderContext.process(data: MarketFaceImpl) {
            collect(ImMsgBody.Elem(marketFace = data.delegate))
            processAlso(PlainText(data.name))
            collect(ImMsgBody.Elem(extraInfo = ImMsgBody.ExtraInfo(flags = 8, groupMask = 1)))
            collectGeneralFlags {
                ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_MARKET_FACE))
            }
        }

        private companion object {
            private val PB_RESERVE_FOR_MARKET_FACE =
                "02 78 80 80 04 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 00 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 04 08 02 10 3B 90 04 80 C0 80 80 04 B8 04 00 C0 04 00 CA 04 00 F8 04 80 80 04 88 05 00".hexToBytes()
        }
    }

    private class DiceEncoder : MessageEncoder<Dice> {
        override suspend fun MessageEncoderContext.process(data: Dice) {
            markAsConsumed()
            processAlso(MarketFaceImpl(data.toJceStruct()))
        }
    }

    private class MarketFaceDecoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val proto = data.marketFace ?: return

            proto.toDiceOrNull()?.let {
                collect(it)
                return
            }

            collect(MarketFaceImpl(proto))
        }
    }


    private companion object {
        /**
         * PC 客户端没有 [ImMsgBody.MarketFace.mobileParam], 是按 [ImMsgBody.MarketFace.faceId] 发的...
         */
        @Suppress("SpellCheckingInspection")
        private val DICE_PC_FACE_IDS = mapOf(
            1 to "E6EEDE15CDFBEB4DF0242448535354F1".hexToBytes(),
            2 to "C5A95816FB5AFE34A58AF0E837A3B5A0".hexToBytes(),
            3 to "382131D722EEA4624F087C5B8035AF5F".hexToBytes(),
            4 to "FA90E956DCAD76742F2DB87723D3B669".hexToBytes(),
            5 to "D51FA892017647431BB243920EC9FB8E".hexToBytes(),
            6 to "7A2303AD80755FCB6BBFAC38327E0C01".hexToBytes(),
        )

        private fun ImMsgBody.MarketFace.toDiceOrNull(): Dice? {
            if (this.tabId != 11464) return null
            val value = when {
                mobileParam.isNotEmpty() -> mobileParam.lastOrNull()?.toInt()?.and(0xff)?.minus(47) ?: return null
                else -> DICE_PC_FACE_IDS.entries.find { it.value.contentEquals(faceId) }?.key ?: return null
            }
            if (value in 1..6) {
                return Dice(value)
            }
            return null
        }

        // From https://github.com/mamoe/mirai/issues/1012
        private fun Dice.toJceStruct(): ImMsgBody.MarketFace {
            return ImMsgBody.MarketFace(
                faceName = byteArrayOf(91, -23, -86, -80, -27, -83, -112, 93),
                itemType = 6,
                faceInfo = 1,
                faceId = byteArrayOf(
                    72, 35, -45, -83, -79, 93,
                    -16, -128, 20, -50, 93, 103,
                    -106, -73, 110, -31
                ),
                tabId = 11464,
                subType = 3,
                key = byteArrayOf(52, 48, 57, 101, 50, 97, 54, 57, 98, 49, 54, 57, 49, 56, 102, 57),
                mediaType = 0,
                imageWidth = 200,
                imageHeight = 200,
                mobileParam = byteArrayOf(
                    114, 115, 99, 84, 121, 112, 101,
                    63, 49, 59, 118, 97, 108, 117,
                    101, 61,
                    (47 + value).toByte()
                ),
                pbReserve = byteArrayOf(
                    10, 6, 8, -56, 1, 16, -56, 1, 64,
                    1, 88, 0, 98, 9, 35, 48, 48, 48,
                    48, 48, 48, 48, 48, 106, 9, 35,
                    48, 48, 48, 48, 48, 48, 48, 48
                )
            )
        }
    }
}
