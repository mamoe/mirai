/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.MarketFace
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

@Serializable
internal data class MarketFaceImpl internal constructor(
    internal val delegate: ImMsgBody.MarketFace,
) : MarketFace {
    @Transient
    override val name: String = delegate.faceName.decodeToString()
    @Transient
    override val id: Int = delegate.tabId

    override fun toString() = "[mirai:marketface:$id,$name]"
}