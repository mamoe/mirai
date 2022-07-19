/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.internal.message.visitor.ex
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.MarketFace
import net.mamoe.mirai.message.data.visitor.MessageVisitor

@SerialName(MarketFace.SERIAL_NAME)
@Serializable
internal data class MarketFaceImpl internal constructor(
    internal val delegate: ImMsgBody.MarketFace,
) : MarketFace {

    override val name: String get() = delegate.faceName.decodeToString()

    @Transient
    override val id: Int = delegate.tabId

    override fun toString() = "[mirai:marketface:$id,$name]"

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitMarketFaceImpl(this, data) ?: super.accept(visitor, data)
    }

    companion object {
        const val SERIAL_NAME = MarketFace.SERIAL_NAME
    }
}