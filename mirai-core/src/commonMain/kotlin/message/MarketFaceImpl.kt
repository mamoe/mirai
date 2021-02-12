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