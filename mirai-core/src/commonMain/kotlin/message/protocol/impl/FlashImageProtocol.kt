/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.message.image.OnlineFriendImageImpl
import net.mamoe.mirai.internal.message.image.OnlineGroupImageImpl
import net.mamoe.mirai.internal.message.image.friendImageId
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.contact
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.hexToBytes

internal class FlashImageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(FlashImage::class, FlashImage.serializer()))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.commonElem == null) return
            if (data.commonElem.serviceType != 3) return

            markAsConsumed()

            val proto =
                data.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype3.serializer())
            if (proto.flashTroopPic != null) {
                collect(FlashImage(OnlineGroupImageImpl(proto.flashTroopPic)))
            }
            if (proto.flashC2cPic != null) {
                collect(FlashImage(OnlineFriendImageImpl(proto.flashC2cPic)))
            }

        }

    }

    private class Encoder : MessageEncoder<FlashImage> {
        override suspend fun MessageEncoderContext.process(data: FlashImage) {
            markAsConsumed()

            collect(data.toJceData(contact))
            processAlso(UNSUPPORTED_FLASH_MESSAGE_PLAIN)
            collectGeneralFlags {
                ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_DOUTU))
            }
        }

        private companion object {
            @Suppress("SpellCheckingInspection")
            private val PB_RESERVE_FOR_DOUTU = "78 00 90 01 01 F8 01 00 A0 02 00 C8 02 00".hexToBytes()
            private val UNSUPPORTED_FLASH_MESSAGE_PLAIN = PlainText("[闪照]请使用新版手机QQ查看闪照。")

            private fun FlashImage.toJceData(messageTarget: ContactOrBot?): ImMsgBody.Elem {
                return if (messageTarget is User) {
                    ImMsgBody.Elem(
                        commonElem = ImMsgBody.CommonElem(
                            serviceType = 3,
                            businessType = 0,
                            pbElem = HummerCommelem.MsgElemInfoServtype3(
                                flashC2cPic = ImMsgBody.NotOnlineImage(
                                    filePath = image.friendImageId,
                                    resId = image.friendImageId,
                                    picMd5 = image.md5,
                                    oldPicMd5 = false,
                                    pbReserve = byteArrayOf(0x78, 0x06)
                                )
                            ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
                        )
                    )
                } else {
                    ImMsgBody.Elem(
                        commonElem = ImMsgBody.CommonElem(
                            serviceType = 3,
                            businessType = 0,
                            pbElem = HummerCommelem.MsgElemInfoServtype3(
                                flashTroopPic = ImMsgBody.CustomFace(
                                    filePath = image.imageId,
                                    picMd5 = image.md5,
                                    pbReserve = byteArrayOf(0x78, 0x06)
                                )
                            ).toByteArray(HummerCommelem.MsgElemInfoServtype3.serializer())
                        )
                    )
                }
            }
        }
    }
}