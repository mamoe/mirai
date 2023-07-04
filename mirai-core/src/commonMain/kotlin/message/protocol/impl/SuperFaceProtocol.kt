/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import kotlin.text.toByteArray

internal class SuperFaceProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(SuperFace::class, SuperFace.serializer()))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.commonElem == null) return
            if (data.commonElem.serviceType != 37) return

            markAsConsumed()

            val proto = data.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype37.serializer())
            collect(SuperFace(proto.qsId))
        }
    }

    private class Encoder : MessageEncoder<SuperFace> {
        override suspend fun MessageEncoderContext.process(data: SuperFace) {
            markAsConsumed()

            collect(ImMsgBody.Elem(commonElem = data.toCommData()))
            processAlso(PlainText("/${data.name}"))
            collectGeneralFlags {
                ImMsgBody.Elem(
                    generalFlags = ImMsgBody.GeneralFlags(
                        pbReserve = ImMsgBody.Text(str = "[${data.name}]请使用最新版手机QQ体验新功能")
                            .toByteArray(ImMsgBody.Text.serializer())
                    )
                )
            }
        }
    }

    companion object {

        fun SuperFace.stickerId(): String {
            return when (id) {
                Face.DA_CALL -> "1"
                Face.BIAN_XING -> "2"
                Face.KE_DAO_LE -> "3"
                Face.ZI_XI_FEN_XI -> "4"
                Face.JIA_YOU -> "5"
                Face.WO_MEI_SHI -> "6"
                Face.CAI_GOU -> "7"
                Face.CHONG_BAI -> "8"
                Face.BI_XIN -> "9"
                Face.QING_ZHU -> "10"
                Face.LAO_SE_PI -> "11"
                Face.CHI_TANG -> "12"
                Face.LAN_QIU -> "13"
                Face.JING_XIA -> "14"
                Face.SHENG_QI -> "15"
                Face.LIU_LEI -> "16"
                Face.DAN_GAO -> "17"
                Face.BIAN_PAO -> "18"
                Face.YAN_HUA -> "19"
                Face.WO_XIANG_KAI_LE -> "20"
                Face.TIAN_PING -> "21"
                Face.HUA_DUO_LIAN -> "22"
                Face.RE_HUA_LE -> "23"
                Face.DA_ZHAO_HU -> "24"
                Face.NI_ZHEN_BANG_BANG -> "25"
                Face.SUAN_Q -> "26"
                Face.WO_FANG_LE -> "27"
                Face.DA_YUAN_ZHONG -> "28"
                Face.HONG_BAO_DUO_DUO -> "29"
                else -> throw UnsupportedOperationException("stickerId with QSid: $id")
            }
        }

        fun SuperFace.stickerType(): Int {
            return when (id) {
                Face.LAN_QIU -> 2
                else -> 1
            }
        }

        fun SuperFace.toCommData(): ImMsgBody.CommonElem {
            return ImMsgBody.CommonElem(
                serviceType = 37,
                pbElem = HummerCommelem.MsgElemInfoServtype37(
                    packId = "1".encodeToByteArray(),
                    stickerId = stickerId().encodeToByteArray(),
                    qsId = id,
                    sourceType = 1,
                    stickerType = stickerType(),
                    text = "/${name}".toByteArray(),
                    randomType = 1
                ).toByteArray(HummerCommelem.MsgElemInfoServtype37.serializer()),
                businessType = stickerType()
            )
        }
    }
}