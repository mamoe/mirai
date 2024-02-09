/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.message.data.transform
import net.mamoe.mirai.internal.message.image.*
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.contact
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePreprocessor
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.CustomFace
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.generateImageId
import net.mamoe.mirai.utils.toUHexString

internal class ImageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(ImageEncoder())
        add(ImageDecoder())

        add(ImagePatcherForGroup())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            @Suppress("DEPRECATION", "DEPRECATION_ERROR")
            add(MessageSerializer(Image::class, Image.Serializer, registerAlsoContextual = true))
        }

        MessageSerializer.superclassesScope(Image::class, MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(OfflineGroupImage::class, OfflineGroupImage.serializer()))
            add(MessageSerializer(OfflineFriendImage::class, OfflineFriendImage.serializer()))
            add(MessageSerializer(OnlineFriendImageImpl::class, OnlineFriendImageImpl.serializer()))
            add(MessageSerializer(OnlineGroupImageImpl::class, OnlineGroupImageImpl.serializer()))
            add(MessageSerializer(OnlineNewTechImageImpl::class, OnlineNewTechImageImpl.serializer()))
        }
    }

    private class ImagePatcherForGroup : OutgoingMessagePreprocessor {
        override suspend fun OutgoingMessagePipelineContext.process() {
            val contact = attributes[CONTACT]
            if (contact !is GroupImpl) return

            val patcher = contact.bot.components[ImagePatcher]
            currentMessageChain = currentMessageChain.transform { element ->
                when (element) {
                    is OfflineGroupImage -> {
                        patcher.patchOfflineGroupImage(contact, element)
                        element
                    }
                    is FriendImage -> {
                        patcher.patchFriendImageToGroupImage(contact, element)
                    }
                    else -> element
                }
            }
        }
    }

    private class ImageDecoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            markAsConsumed()
            when {
                data.notOnlineImage != null -> {
                    collect(OnlineFriendImageImpl(data.notOnlineImage))
                }
                data.customFace != null -> {
                    collect(OnlineGroupImageImpl(data.customFace))
                    data.customFace.pbReserve.let {
                        if (it.isNotEmpty() && it.loadAs(CustomFace.ResvAttr.serializer()).msgImageShow != null) {
                            collect(ShowImageFlag)
                        }
                    }
                }
                data.commonElem != null && data.commonElem.serviceType == 48 -> {
                    collect(OnlineNewTechImageImpl(data.commonElem))
                }
                else -> {
                    markNotConsumed()
                }
            }
        }

    }

    private class ImageEncoder : MessageEncoder<AbstractImage> {
        override suspend fun MessageEncoderContext.process(data: AbstractImage) {
            markAsConsumed()

            when (data) {
                is OfflineGroupImage -> {
                    if (contact is User) {
                        collect(ImMsgBody.Elem(notOnlineImage = data.toJceData().toNotOnlineImage()))
                    } else {
                        collect(ImMsgBody.Elem(customFace = data.toJceData()))
                    }
                }
                is OnlineGroupImageImpl -> {
                    if (contact is User) {
                        collect(ImMsgBody.Elem(notOnlineImage = data.delegate.toNotOnlineImage()))
                    } else {
                        collect(ImMsgBody.Elem(customFace = data.delegate))
                    }
                }
                is OnlineFriendImageImpl -> {
                    if (contact is User) {
                        collect(ImMsgBody.Elem(notOnlineImage = data.delegate))
                    } else {
                        collect(ImMsgBody.Elem(customFace = data.delegate.toCustomFace()))
                    }
                }
                is OfflineFriendImage -> {
                    if (contact is User) {
                        collect(ImMsgBody.Elem(notOnlineImage = data.toJceData()))
                    } else {
                        collect(ImMsgBody.Elem(customFace = data.toJceData().toCustomFace()))
                    }
                }
                is OnlineNewTechImageImpl -> {
                    collect(ImMsgBody.Elem(commonElem = data.commonElem))
                }
            }
        }

        companion object {
            private fun OfflineGroupImage.toJceData(): ImMsgBody.CustomFace {
                return ImMsgBody.CustomFace(
                    fileId = this.fileId ?: 0,
                    filePath = this.imageId,
                    picMd5 = this.md5,
                    flag = ByteArray(4),
                    size = size.toInt(),
                    width = width.coerceAtLeast(1),
                    height = height.coerceAtLeast(1),
                    imageType = getIdByImageType(imageType),
                    origin = if (imageType == ImageType.GIF) {
                        0
                    } else {
                        1
                    },
                    //_400Height = 235,
                    //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
                    //_400Width = 351,
                    //        pbReserve = "08 00 10 00 32 00 50 00 78 08".autoHexToBytes(),
                    bizType = 5,
                    fileType = 66,
                    useful = 1,
                    //  pbReserve = CustomFaceExtPb.ResvAttr().toByteArray(CustomFaceExtPb.ResvAttr.serializer())
                )
            }

            private fun ImMsgBody.CustomFace.toNotOnlineImage(): ImMsgBody.NotOnlineImage {
                val resId = calculateResId()

                return ImMsgBody.NotOnlineImage(
                    filePath = filePath,
                    resId = resId,
                    oldPicMd5 = false,
                    picWidth = width,
                    picHeight = height,
                    imgType = imageType,
                    picMd5 = picMd5,
                    fileLen = size.toLong(),
                    oldVerSendFile = oldData,
                    downloadPath = resId,
                    original = origin,
                    bizType = bizType,
                    pbReserve = byteArrayOf(0x78, 0x02),
                )
            }

            private fun ImMsgBody.NotOnlineImage.toCustomFace(): ImMsgBody.CustomFace {
                return ImMsgBody.CustomFace(
                    filePath = generateImageId(picMd5, getImageType(imgType)),
                    picMd5 = picMd5,
                    bizType = 5,
                    fileType = 66,
                    useful = 1,
                    flag = ByteArray(4),
                    bigUrl = bigUrl,
                    origUrl = origUrl,
                    width = picWidth.coerceAtLeast(1),
                    height = picHeight.coerceAtLeast(1),
                    imageType = imgType,
                    //_400Height = 235,
                    //_400Url = "/gchatpic_new/000000000/1041235568-2195821338-01E9451B70EDEAE3B37C101F1EEBF5B5/400?term=2",
                    //_400Width = 351,
                    origin = original,
                    size = fileLen.toInt()
                )
            }

            // aka friend image id
            private fun ImMsgBody.NotOnlineImageOrCustomFace.calculateResId(): String {
                val url = origUrl.takeIf { it.isNotBlank() }
                    ?: thumbUrl.takeIf { it.isNotBlank() }
                    ?: _400Url.takeIf { it.isNotBlank() }
                    ?: ""

                // gchatpic_new
                // offpic_new
                val picSenderId = url.substringAfter("pic_new/").substringBefore("/")
                    .takeIf { it.isNotBlank() } ?: "000000000"
                val unknownInt = url.substringAfter("-").substringBefore("-")
                    .takeIf { it.isNotBlank() } ?: "000000000"

                return "/$picSenderId-$unknownInt-${picMd5.toUHexString("")}"
            }


            private fun OfflineFriendImage.toJceData(): ImMsgBody.NotOnlineImage {
                val friendImageId = this.friendImageId
                return ImMsgBody.NotOnlineImage(
                    filePath = friendImageId,
                    resId = friendImageId,
                    oldPicMd5 = false,
                    picMd5 = this.md5,
                    fileLen = size,
                    downloadPath = friendImageId,
                    original = if (imageType == ImageType.GIF) {
                        0
                    } else {
                        1
                    },
                    picWidth = width,
                    picHeight = height,
                    imgType = getIdByImageType(imageType),
                    pbReserve = byteArrayOf(0x78, 0x02)
                )
            }

        }
    }
}