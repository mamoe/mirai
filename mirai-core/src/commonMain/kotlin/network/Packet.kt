/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.utils.MiraiLogger

/*

// moved to `mirai-core`

/**
 * 从服务器收到的包解析之后的结构化数据.
 * 它是一个数据包工厂的处理的返回值.
 */
interface Packet {
    /**
     * 实现这个接口的包将不会被记录到日志中
     */
    interface NoLog
}
*/

/**
 * PacketFactory 可以一次解析多个包出来. 它们将会被分别广播.
 */
internal interface MultiPacket : Packet, Collection<Packet>

internal fun Collection<Packet>.toPacket(): Packet {
    return when (this.size) {
        1 -> this.single()
        else -> MultiPacketImpl(this)
    }
}

internal fun MultiPacket(delegate: Collection<Packet>): MultiPacket = MultiPacketImpl(delegate)

internal open class MultiPacketImpl(
    val delegate: Collection<Packet>
) : MultiPacket, Collection<Packet> by delegate {

    override fun toString(): String = delegate.joinToString(
        separator = "\n",
        prefix = "MultiPacket [\n",
        postfix = "]",
    )
}


internal class ParseErrorPacket(
    val error: Throwable,
    val direction: Direction = Direction.TO_BOT_LOGGER,
) : Packet, Packet.NoLog {
    enum class Direction {
        TO_BOT_LOGGER {
            override fun getLogger(bot: AbstractBot): MiraiLogger = bot.logger
        },
        TO_NETWORK_LOGGER {
            override fun getLogger(bot: AbstractBot): MiraiLogger = bot.network.logger
        };

        abstract fun getLogger(bot: AbstractBot): MiraiLogger
    }
}
