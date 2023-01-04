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
import net.mamoe.mirai.utils.copy
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.map


internal class MarketFaceProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(DiceEncoder())
        add(RockPaperScissorsEncoder())
        add(MarketFaceImplEncoder())

        add(MarketFaceDecoder())


        // Serialization overview:
        // Using MarketFace as serial type:
        // - convert data to MarketFaceImpl on serialization. Convert them back to subtypes on deserialization.
        // - serial name is always "MarketFace"
        // Using subtypes:
        // - serial name is name of subtype, i.e. "MarketFace" / "Dice".
        // - Note that we don't use MarketFaceImpl but MarketFace for compatibility concerns.

        add(
            MessageSerializer(
                MarketFace::class, MarketFaceImpl.serializer().map(
                    resultantDescriptor = MarketFaceImpl.serializer().descriptor.copy(MarketFace.SERIAL_NAME),
                    deserialize = {
                        it.delegate.toDiceOrNull() ?: it.delegate.toRockPaperScissorsOrNull() ?: it
                    },
                    serialize = {
                        when (it) {
                            is Dice -> MarketFaceImpl(it.toJceStruct())
                            is RockPaperScissors -> MarketFaceImpl(it.toJceStruct())
                            is MarketFaceImpl -> it
                            else -> {
                                error("Unsupported MarketFace type ${it::class.qualifiedName}")
                            }
                        }
                    }
                ), emptyArray()
            )
        )

        MessageSerializer.superclassesScope(MarketFace::class, MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(MarketFaceImpl::class, MarketFaceImpl.serializer()))
            add(MessageSerializer(Dice::class, Dice.serializer()))
            add(MessageSerializer(RockPaperScissors::class, RockPaperScissors.serializer()))
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

    private class RockPaperScissorsEncoder : MessageEncoder<RockPaperScissors> {
        override suspend fun MessageEncoderContext.process(data: RockPaperScissors) {
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

            proto.toRockPaperScissorsOrNull()?.let {
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

        private val RPS_PC_FACE_IDS = mapOf(
            48 to "E5D889F1DF79B2B45183F625584465D3".hexToBytes(),
            49 to "628FA4AB7B6C2BCCFCDCD0C2DAF7A60C".hexToBytes(),
            50 to "457CDE420F598EB424CED2E905D38D8B".hexToBytes(),
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

        private fun ImMsgBody.MarketFace.toRockPaperScissorsOrNull(): RockPaperScissors? {
            if (tabId != 11415) return null

            val value = when {
                mobileParam.isNotEmpty() -> {
                    val theLast = mobileParam.lastOrNull() ?: return null
                    theLast.toInt().and(0xff)
                }
                else -> RPS_PC_FACE_IDS.entries.find { it.value.contentEquals(faceId) }?.key ?: return null
            }

            return when (value) {
                48 -> RockPaperScissors.ROCK
                49 -> RockPaperScissors.SCISSORS
                50 -> RockPaperScissors.PAPER

                else -> null
            }
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

        private fun RockPaperScissors.toJceStruct(): ImMsgBody.MarketFace {
            return ImMsgBody.MarketFace(
                faceName = byteArrayOf(91, -25, -116, -100, -26, -117, -77, 93),
                itemType = 6,
                faceInfo = 1,
                faceId = byteArrayOf(
                    -125, -56, -94, -109, -82,
                    101, -54, 20, 15, 52,
                    -127, 32, -89, 116, 72, -18
                ),
                tabId = 11415,
                subType = 3,
                key = byteArrayOf(55, 100, 101, 51, 57, 102, 101, 98, 99, 102, 52, 53, 101, 54, 100, 98),
                mediaType = 0,
                imageWidth = 200,
                imageHeight = 200,
                mobileParam = byteArrayOf(
                    114, 115, 99, 84, 121, 112, 101,
                    63, 49, 59, 118, 97, 108, 117,
                    101, 61,
                    internalId
                ),
                pbReserve = byteArrayOf(10, 6, 8, -56, 1, 16, -56, 1, 64, 1)
            )
        }
    }
}
