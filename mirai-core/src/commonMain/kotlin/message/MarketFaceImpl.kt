/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.MarketFace

@SerialName(MarketFace.SERIAL_NAME)
@Serializable
internal data class MarketFaceImpl internal constructor(
    internal val delegate: ImMsgBody.MarketFace,
) : MarketFace {

    override val name: String get() = delegate.faceName.decodeToString()

    @Transient
    override val id: Int = delegate.tabId

    override fun toString() = "[mirai:marketface:$id,$name]"
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

private fun ImMsgBody.MarketFace.toDiceOrNull(): Dice? {
    if (this.tabId != 11464) return null
    val value = mobileParam.lastOrNull()?.toInt()?.and(0xff)?.minus(47) ?: 0
    if (value in 1..6) {
        return Dice(value)
    }
    return null
}