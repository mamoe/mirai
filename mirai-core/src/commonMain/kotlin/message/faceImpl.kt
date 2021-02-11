/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.io.core.toByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toByteArray

internal val FACE_BUF = "00 01 00 04 52 CC F5 D0".hexToBytes()

internal fun Face.toJceData(): ImMsgBody.Face {
    return ImMsgBody.Face(
        index = this.id,
        old = (0x1445 - 4 + this.id).toShort().toByteArray(),
        buf = FACE_BUF
    )
}

internal fun Face.toCommData(): ImMsgBody.CommonElem {
    return ImMsgBody.CommonElem(
        serviceType = 33,
        pbElem = HummerCommelem.MsgElemInfoServtype33(
            index = this.id,
            name = "/${this.name}".toByteArray(),
            compat = "/${this.name}".toByteArray()
        ).toByteArray(HummerCommelem.MsgElemInfoServtype33.serializer()),
        businessType = 1
    )

}

@SerialName(MarketFace.SERIAL_NAME)
@Serializable
internal data class MarketFaceImpl internal constructor(
    internal val delegate: ImMsgBody.MarketFace,
) : MarketFace, RefinableMessage {

    companion object {
        @MiraiInternalApi
        internal fun transform(template: MarketFaceTemplate): MarketFaceImpl? = when (template) {
            is Dice -> {
                //数据来源于 https://github.com/mamoe/mirai/issues/1012
                MarketFaceImpl(
                    ImMsgBody.MarketFace(
                        faceName = byteArrayOf(91, -23, -102, -113, -26, -100, -70, -23, -86, -80, -27, -83, -112, 93),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = byteArrayOf(
                            72,
                            35,
                            -45,
                            -83,
                            -79,
                            93,
                            -16,
                            -128,
                            20,
                            -50,
                            93,
                            103,
                            -106,
                            -73,
                            110,
                            -31
                        ),
                        tabId = 11464,
                        subType = 3,
                        key = byteArrayOf(52, 48, 57, 101, 50, 97, 54, 57, 98, 49, 54, 57, 49, 56, 102, 57),
                        mediaType = 0,
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = byteArrayOf(
                            114,
                            115,
                            99,
                            84,
                            121,
                            112,
                            101,
                            63,
                            49,
                            59,
                            118,
                            97,
                            108,
                            117,
                            101,
                            61,
                            (47 + template.value).toByte()
                        ),
                        pbReserve = byteArrayOf(10, 6, 8, -56, 1, 16, -56, 1, 64, 1)
                    )
                )
            }
            else -> null
        }
    }

    override val name: String get() = delegate.faceName.decodeToString()

    @Transient
    override val id: Int = delegate.tabId
    override suspend fun refine(contact: Contact, context: MessageChain): Message =
        when (id) {
            11464 -> {
                kotlin.runCatching { MarketFace.dice(delegate.mobileParam.last().minus(47)) }.getOrDefault(this)
            }
            else -> this
        }

    override fun toString() = "[mirai:marketface:$id,$name]"

}